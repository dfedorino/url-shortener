package com.dfedorino.urlshortener.jdbc.repository;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataFactory {

  public static final String ORIGINAL_URL = "originalUrl";
  public static final int REDIRECT_LIMIT = 1;

  public String randomLinkCode() {
    return UUID.randomUUID().toString().substring(0, 10);
  }

  public Link newLink(Consumer<Link> overrides) {
    Link link = new Link(
        null,
        LinkStatus.ACTIVE.getId(),
        randomLinkCode(),
        ORIGINAL_URL,
        REDIRECT_LIMIT
    );
    overrides.accept(link);
    return link;
  }
}
