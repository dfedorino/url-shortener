package com.dfedorino.urlshortener;

import com.dfedorino.urlshortener.config.AppConfig;
import com.dfedorino.urlshortener.service.housekeeping.LinkHousekeepingService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.util.DatabaseUtil;
import com.dfedorino.urlshortener.util.PropertiesUtil;
import java.io.IOException;
import javax.sql.DataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UrlShortenerApp {

    public static void main(String[] args) throws IOException {
        LinkHousekeepingService linkHousekeepingService = null;

        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(AppConfig.class);
            PropertiesUtil.addApplicationProperties(context, "application.properties");
            context.refresh();

            DataSource dataSource = context.getBean(DataSource.class);
            DatabaseUtil.preloadDataFromClasspath("schema.sql", dataSource);

            Cli cli = context.getBean(Cli.class);
            linkHousekeepingService = context.getBean(LinkHousekeepingService.class);
            linkHousekeepingService.start();
            cli.start();
        } finally {
            if (linkHousekeepingService != null) {
                linkHousekeepingService.stop();
            }
        }
    }
}
