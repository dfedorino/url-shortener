package com.dfedorino.urlshortener.service.validation.config;

import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LinkValidationConfig {

  @Bean
  public LinkValidationService linkValidationService(Environment env) {
    return new LinkValidationService(
        Long.parseLong(Objects.requireNonNull(env.getProperty("ttl"))),
        Clock.system(ZoneId.of("Europe/Moscow"))
    );
  }
}
