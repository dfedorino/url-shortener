package com.dfedorino.urlshortener.jdbc.repository;

import com.dfedorino.urlshortener.jdbc.connection.HikariConfiguration;
import com.dfedorino.urlshortener.jdbc.repository.config.JdbcRepositoryConfig;
import com.dfedorino.urlshortener.util.DatabaseUtil;
import com.dfedorino.urlshortener.util.PropertiesUtil;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

public class AbstractJdbcRepositoryTest {

  protected AnnotationConfigApplicationContext ctx;
  protected TransactionTemplate tx;

  @BeforeEach
  void setUp() throws IOException {
    ctx = new AnnotationConfigApplicationContext();
    ctx.register(HikariConfiguration.class, JdbcRepositoryConfig.class);
    PropertiesUtil.addApplicationProperties(ctx, "test.properties");
    ctx.refresh();

    DatabaseUtil.preloadDataFromClasspath("schema.sql", ctx.getBean(DataSource.class));

    tx = ctx.getBean(TransactionTemplate.class);
  }

  @AfterEach
  void tearDown() {
    DatabaseUtil.dropAllObjects(ctx.getBean(DataSource.class));
  }

  protected <T> T tx(Callable<T> workload) {
    return Objects.requireNonNull(tx.execute($ -> {
      try {
        return workload.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }));
  }

  protected void tx(Runnable workload) {
    tx.executeWithoutResult($ -> workload.run());
  }
}
