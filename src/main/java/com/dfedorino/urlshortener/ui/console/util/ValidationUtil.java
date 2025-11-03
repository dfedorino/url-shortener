package com.dfedorino.urlshortener.ui.console.util;

import com.dfedorino.urlshortener.ui.console.Cli;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {

  public boolean isValidShortLink(String link) {
    return link.startsWith(Cli.SHORTENED_URL_PREFIX);
  }

  public boolean isValidUrl(String url) {
    try {
      return URI.create(url).toURL() != null;
    } catch (MalformedURLException e) {
      return false;
    }
  }
}
