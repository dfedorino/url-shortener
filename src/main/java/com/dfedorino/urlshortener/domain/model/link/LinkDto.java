package com.dfedorino.urlshortener.domain.model.link;

import java.time.LocalDateTime;

public record LinkDto(
        Long userId,
        String status,
        String code,
        String originalUrl,
        Integer redirectLimit,
        LocalDateTime createdAt
) {

    public static LinkDto of(Link link) {
        return new LinkDto(
                link.getUserId(),
                LinkStatus.ofId(link.getStatusId()).orElseThrow().name(),
                link.getCode(),
                link.getOriginalUrl(),
                link.getRedirectLimit(),
                link.getCreatedAt()
        );
    }
}
