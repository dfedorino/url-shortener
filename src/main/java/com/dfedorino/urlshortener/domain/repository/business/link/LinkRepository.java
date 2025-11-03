package com.dfedorino.urlshortener.domain.repository.business.link;

import com.dfedorino.urlshortener.domain.model.link.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    Link save(Link link);

    Optional<Link> findByUserIdAndCode(Long userId, String code);

    List<Link> findByUserId(Long userId);

    List<Link> findByUserIdAndStatusId(Long userId, Long statusId);

    boolean updateOriginalUrlByUserIdAndCode(Long userId, String code, String newOriginalUrl);

    boolean updateRedirectLimitByUserIdAndCode(Long userId, String code, Integer newRedirectLimit);

    boolean updateStatusIdByUserIdAndCode(Long userId, String code, Long newStatusId);

    List<Link> findByStatusId(Long id);

    Optional<Link> findByUserIdAndOriginalUrl(Long userId, String originalUrl);
}
