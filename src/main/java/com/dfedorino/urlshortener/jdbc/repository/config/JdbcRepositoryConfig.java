package com.dfedorino.urlshortener.jdbc.repository.config;

import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.domain.repository.housekeeping.HousekeepingLinkRepository;
import com.dfedorino.urlshortener.jdbc.repository.business.link.JdbcLinkCounterRepository;
import com.dfedorino.urlshortener.jdbc.repository.business.link.JdbcLinkRepository;
import com.dfedorino.urlshortener.jdbc.repository.business.user.JdbcUserRepository;
import com.dfedorino.urlshortener.jdbc.repository.housekeeping.link.JdbcHousekeepingLinkRepository;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class JdbcRepositoryConfig {

  @Bean
  public UserRepository jdbcUserRepository(DataSource dataSource) {
    return new JdbcUserRepository(JdbcClient.create(dataSource));
  }

  @Bean
  public LinkRepository jdbcLinkRepository(DataSource dataSource) {
    return new JdbcLinkRepository(JdbcClient.create(dataSource));
  }

  @Bean
  public HousekeepingLinkRepository jdbcHousekeepingLinkRepository(DataSource dataSource) {
    return new JdbcHousekeepingLinkRepository(JdbcClient.create(dataSource));
  }

  @Bean
  public JdbcLinkCounterRepository jdbcLinkCounterRepository(DataSource dataSource) {
    return new JdbcLinkCounterRepository(JdbcClient.create(dataSource));
  }

  @Bean
  public TransactionTemplate transactionTemplate(DataSource dataSource) {
    return new TransactionTemplate(new JdbcTransactionManager(dataSource));
  }
}
