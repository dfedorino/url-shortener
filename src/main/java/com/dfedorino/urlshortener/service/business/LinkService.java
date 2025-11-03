package com.dfedorino.urlshortener.service.business;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkCounterRepository;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.service.business.encode.IdEncodingStrategy;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService.ValidatedLink;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final LinkCounterRepository linkCounterRepository;
    private final IdEncodingStrategy idEncodingStrategy;
    private final LinkValidationService linkValidationService;
    private final TransactionTemplate tx;

    public LinkDto createLink(@NonNull Long userId, @NonNull String originalUrl,
                              @NonNull Integer redirectLimit) {
        return tx.execute($ -> {
            Long codeId = linkCounterRepository.incrementAndGet();
            return LinkDto.of(linkRepository.save(new Link(
                    userId,
                    LinkStatus.ACTIVE.getId(),
                    idEncodingStrategy.encode(codeId),
                    originalUrl,
                    redirectLimit
            )));
        });
    }

    public Optional<LinkValidationService.ValidatedLink> findValidatedLink(@NonNull Long userId,
                                                                           @NonNull String code) {
        return safeTx($ -> linkRepository.findByUserIdAndCode(userId, code))
                .map(linkValidationService::validate);
    }

    public List<LinkDto> findValidUserLinks(@NonNull Long userId) {
        return safeTx(
                $ -> linkRepository.findByUserIdAndStatusId(userId, LinkStatus.ACTIVE.getId()))
                .stream()
                .map(LinkDto::of)
                .toList();
    }

    public Optional<LinkDto> updateOriginalUrl(@NonNull Long userId, @NonNull String code,
                                               @NonNull String newOriginalUrl) {
        boolean isUpdatedSuccessfully = safeTx($ ->
                                                       linkRepository.updateOriginalUrlByUserIdAndCode(
                                                               userId, code, newOriginalUrl));
        return isUpdatedSuccessfully ?
                safeTx($ -> linkRepository.findByUserIdAndCode(userId, code)).map(LinkDto::of) :
                Optional.empty();
    }

    public Optional<LinkDto> updateRedirectLimit(@NonNull Long userId, @NonNull String code,
                                                 @NonNull Integer newRedirectLimit) {
        boolean isUpdatedSuccessfully = safeTx($ ->
                                                       linkRepository.updateRedirectLimitByUserIdAndCode(
                                                               userId, code, newRedirectLimit));
        return isUpdatedSuccessfully ?
                safeTx($ -> linkRepository.findByUserIdAndCode(userId, code)).map(LinkDto::of) :
                Optional.empty();
    }

    public Optional<LinkDto> invalidateLink(@NonNull Long userId, @NonNull String code) {
        return updateLinkStatus(userId, code, LinkStatus.INVALID);
    }

    // TODO: write test
    public Optional<LinkDto> deleteLink(@NonNull Long userId, @NonNull String code) {
        return updateLinkStatus(userId, code, LinkStatus.DELETED);
    }

    private Optional<LinkDto> updateLinkStatus(@NonNull Long userId, @NonNull String code,
                                               @NonNull LinkStatus newStatus) {
        boolean isUpdatedSuccessfully = safeTx($ ->
                                                       linkRepository.updateStatusIdByUserIdAndCode(
                                                               userId, code, newStatus.getId()));
        return isUpdatedSuccessfully ?
                safeTx($ -> linkRepository.findByUserIdAndCode(userId, code)).map(LinkDto::of) :
                Optional.empty();
    }

    private <T> T safeTx(TransactionCallback<T> callback) {
        return Objects.requireNonNull(tx.execute(callback));
    }

    public Optional<ValidatedLink> findValidatedLinkByOriginalUrl(Long userId, String originalUrl) {
        return linkRepository.findByUserIdAndOriginalUrl(userId, originalUrl)
                .map(linkValidationService::validate);
    }
}
