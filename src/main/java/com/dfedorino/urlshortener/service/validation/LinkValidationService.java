package com.dfedorino.urlshortener.service.validation;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinkValidationService {

    public static final String EXPIRED = "Link expired";
    public static final String REDIRECT_LIMIT_REACHED = "Link redirect limit reached";
    @NonNull
    private final Long ttl;
    @NonNull
    private final Clock clock;

    public ValidatedLink validate(Link link) {
        LocalDateTime validUntil = link.getCreatedAt().plus(ttl, ChronoUnit.MILLIS);
        LocalDateTime now = LocalDateTime.now(clock);

        if (validUntil.isBefore(now)) {
            return ValidatedLink.invalid(link, EXPIRED);
        }
        if (link.getRedirectLimit() == 0) {
            return ValidatedLink.invalid(link, REDIRECT_LIMIT_REACHED);
        }
        return ValidatedLink.valid(link);
    }

    public enum Status {
        VALID, INVALID
    }

    public record ValidatedLink(
            Status status,
            LinkDto link,
            String reasonWhyInvalid
    ) {

        public static ValidatedLink valid(Link validLink) {
            return new ValidatedLink(Status.VALID, LinkDto.of(validLink), null);
        }

        public static ValidatedLink invalid(Link validLink, String reasonWhyInvalid) {
            return new ValidatedLink(Status.INVALID, LinkDto.of(validLink), reasonWhyInvalid);
        }
    }
}
