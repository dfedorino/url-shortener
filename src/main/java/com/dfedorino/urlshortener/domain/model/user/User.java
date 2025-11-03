package com.dfedorino.urlshortener.domain.model.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private Long id;
  private String uuid;
  private LocalDateTime createdAt;

  public User(String uuid) {
    this(null, uuid, null);
  }
}
