package com.dfedorino.urlshortener.service.validation;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.service.validation.dto.ValidatedLink;
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

}
