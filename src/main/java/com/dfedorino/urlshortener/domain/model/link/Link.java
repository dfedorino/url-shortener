package com.dfedorino.urlshortener.domain.model.link;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {

  private Long id;
  private Long userId;
  private Long statusId;
  private String code;
  private String originalUrl;
  private Integer redirectLimit;
  private LocalDateTime createdAt;

  public Link(Long userId, Long statusId, String code, String originalUrl, Integer redirectLimit) {
    this(null, userId, statusId, code, originalUrl, redirectLimit, null);
  }
}
