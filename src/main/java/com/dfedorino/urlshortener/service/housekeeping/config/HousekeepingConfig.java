package com.dfedorino.urlshortener.service.housekeeping.config;

import com.dfedorino.urlshortener.service.housekeeping.LinkHousekeepingService;
import com.dfedorino.urlshortener.service.housekeeping.cleanup.CleanupStrategy;
import java.util.Objects;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class HousekeepingConfig {

  @Bean
  public LinkHousekeepingService linkHousekeepingService(CleanupStrategy cleanupStrategy,
      Environment env) {
    return new LinkHousekeepingService(
        cleanupStrategy,
        Executors.newSingleThreadScheduledExecutor(),
        Long.parseLong(Objects.requireNonNull(env.getProperty("housekeeping.interval")))
    );
  }
}
