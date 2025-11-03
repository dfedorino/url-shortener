package com.dfedorino.urlshortener.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

@UtilityClass
public class PropertiesUtil {

  public static void addApplicationProperties(AnnotationConfigApplicationContext context,
      String path) throws IOException {
    ConfigurableEnvironment environment = context.getEnvironment();
    MutablePropertySources propertySources = environment.getPropertySources();
    propertySources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
    propertySources.addFirst(new ResourcePropertySource(new ClassPathResource(path)));
  }

  @SneakyThrows
  public Properties readProperties(String path) {
    var properties = new Properties();
    properties.load(new FileReader(path));
    return properties;
  }
}
