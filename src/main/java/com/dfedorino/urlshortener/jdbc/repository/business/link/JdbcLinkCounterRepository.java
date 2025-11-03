package com.dfedorino.urlshortener.jdbc.repository.business.link;

import com.dfedorino.urlshortener.domain.repository.business.link.LinkCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcLinkCounterRepository implements LinkCounterRepository {

  public static final String INCREMENT_COUNTER = "UPDATE link_counter SET counter = counter + 1 WHERE id = 1";
  public static final String SELECT_BY_ID = "SELECT counter FROM link_counter WHERE id = 1";
  private final JdbcClient jdbcClient;

  @Override
  public Long incrementAndGet() {
    jdbcClient.sql(INCREMENT_COUNTER)
        .update();
    return jdbcClient.sql(SELECT_BY_ID)
        .query(Long.class)
        .single();
  }
}
