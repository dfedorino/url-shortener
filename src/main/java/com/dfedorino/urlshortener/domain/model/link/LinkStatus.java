package com.dfedorino.urlshortener.domain.model.link;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkStatus {
  ACTIVE(1L), INVALID(2L), DELETED(3L);
  private final Long id;

  public static Optional<LinkStatus> ofId(Long statusId) {
    return Arrays.stream(LinkStatus.values())
        .filter(status -> Objects.equals(status.getId(), statusId))
        .findAny();
  }
}
