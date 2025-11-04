package com.dfedorino.urlshortener.service.validation.dto;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.service.validation.ValidationStatus;

public record ValidatedLink(
        ValidationStatus validationStatus,
        LinkDto link,
        String reasonWhyInvalid
) {

    public static ValidatedLink valid(Link validLink) {
        return new ValidatedLink(ValidationStatus.VALID, LinkDto.of(validLink), null);
    }

    public static ValidatedLink invalid(Link validLink, String reasonWhyInvalid) {
        return new ValidatedLink(ValidationStatus.INVALID, LinkDto.of(validLink), reasonWhyInvalid);
    }
}
