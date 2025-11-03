package com.dfedorino.urlshortener.jdbc.repository.business.link;

import static org.assertj.core.api.Assertions.assertThat;

import com.dfedorino.urlshortener.domain.repository.business.link.LinkCounterRepository;
import com.dfedorino.urlshortener.jdbc.repository.AbstractJdbcRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcLinkCounterRepositoryTest extends AbstractJdbcRepositoryTest {

  private LinkCounterRepository repository;

  @BeforeEach
  void setUp() {
    repository = ctx.getBean(LinkCounterRepository.class);
    ;
  }

  @Test
  void incrementAndGet() {
    Long one = tx(() -> repository.incrementAndGet());
    assertThat(one).isEqualTo(1L);

    Long two = tx(() -> repository.incrementAndGet());
    assertThat(two).isEqualTo(2L);

    Long three = tx(() -> repository.incrementAndGet());
    assertThat(three).isEqualTo(3L);
  }
}