package com.dfedorino.urlshortener.config;

import com.dfedorino.urlshortener.UrlShortenerApp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = UrlShortenerApp.class)
public class AppConfig {

}
