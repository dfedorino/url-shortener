package com.dfedorino.urlshortener.jdbc.util;

import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@UtilityClass
public class DataUtil {

    public void preloadSchema(DataSource dataSource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource("schema.sql"));
        resourceDatabasePopulator.execute(dataSource);
    }
}
