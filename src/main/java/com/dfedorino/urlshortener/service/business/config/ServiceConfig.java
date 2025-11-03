package com.dfedorino.urlshortener.service.business.config;

import com.dfedorino.urlshortener.domain.repository.business.link.LinkCounterRepository;
import com.dfedorino.urlshortener.domain.repository.business.link.LinkRepository;
import com.dfedorino.urlshortener.domain.repository.business.user.UserRepository;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.service.business.encode.IdEncodingStrategy;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ServiceConfig {

  @Bean
  public LinkService linkService(
      LinkRepository linkRepository,
      LinkCounterRepository linkCounterRepository,
      IdEncodingStrategy idEncodingStrategy,
      LinkValidationService linkValidationService,
      TransactionTemplate transactionTemplate
  ) {
    return new LinkService(linkRepository,
        linkCounterRepository,
        idEncodingStrategy,
        linkValidationService,
        transactionTemplate
    );
  }

  @Bean
  public UserService userService(UserRepository userRepository,
      TransactionTemplate transactionTemplate) {
    return new UserService(userRepository, transactionTemplate);
  }
}
