package com.dfedorino.urlshortener.jdbc.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class HikariConfiguration {

    @Bean
    public HikariConfig hikariConfig(Environment env) {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Objects.requireNonNull(env.getProperty("jdbc.url")));
        hikariConfig.setUsername(Objects.requireNonNull(env.getProperty("jdbc.username")));
        hikariConfig.setPassword(Objects.requireNonNull(env.getProperty("jdbc.password")));
        int maxPoolSize = Integer.parseInt(
                Objects.requireNonNull(env.getProperty("jdbc.max-pool-size")));
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        boolean autoCommit = Boolean.parseBoolean(
                Objects.requireNonNull(env.getProperty("jdbc.autocommit")));
        hikariConfig.setAutoCommit(autoCommit);
        return hikariConfig;
    }

    @Bean
    public DataSource hikariDataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }
}
