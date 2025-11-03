package com.dfedorino.urlshortener.domain.model.link;

import java.time.LocalDateTime;

public record LinkDTO(
    Long userId,
    String status,
    String code,
    String originalUrl,
    Integer redirectLimit,
    LocalDateTime createdAt
) {

  public static LinkDTO of(Link link) {
    return new LinkDTO(
        link.getUserId(),
        LinkStatus.ofId(link.getStatusId()).orElseThrow().name(),
        link.getCode(),
        link.getOriginalUrl(),
        link.getRedirectLimit(),
        link.getCreatedAt()
    );
  }
}
