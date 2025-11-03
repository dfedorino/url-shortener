package com.dfedorino.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.dfedorino.urlshortener.domain.model.link.LinkDTO;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.jdbc.util.DataUtil;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.util.DatabaseUtil;
import com.dfedorino.urlshortener.util.PropertiesUtil;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
class IntegrationTest {

  private AnnotationConfigApplicationContext ctx;

  private Command.CreateLink createLink;
  private Command.Redirect redirect;
  private Command.ListActiveLinks listActiveLinks;
  private Command.EditLinkUrl editLinkUrl;
  private Command.EditLinkRedirectLimit editLinkRedirectLimit;
  private Command.Login login;

  private ArgumentCaptor<URI> uriArgumentCaptor;


  @BeforeEach
  void setUp() throws IOException {
    initContextWithProperties("test.properties");
    initCommandBeans();
    uriArgumentCaptor = ArgumentCaptor.forClass(URI.class);

    DataUtil.preloadSchema(ctx.getBean(DataSource.class));
  }

  @AfterEach
  void tearDown() {
    DatabaseUtil.dropAllObjects(ctx.getBean(DataSource.class));
  }

  /**
   * 1. Убедитесь, что одна и та же ссылка, сокращенная разными пользователями, генерирует
   * уникальные короткие ссылки.
   */
  @Test
  void same_url_different_users() {
    var link1 = createLink.apply(Command.CreateLink.KEY, "https://skillfactory.ru/", "100");
    String user1 = Cli.USER_UUID.get();

    // emulate new session
    Cli.USER_UUID.remove();

    var link2 = createLink.apply(Command.CreateLink.KEY, "https://skillfactory.ru/", "100");
    String user2 = Cli.USER_UUID.get();

    assertThat(user1).isNotEqualTo(user2);
    assertThat(link1.originalUrl()).isEqualTo(link2.originalUrl());
    assertThat(link1.code()).isNotEqualTo(link2.code());
  }

  /**
   * 2. Проверьте, что при исчерпании лимита переходов переход по ссылке блокируется. 4.1 Убедитесь,
   * что пользователи получают уведомления о том, что их ссылка недоступна из-за исчерпания лимита
   */
  @Test
  void check_redirect_limit() {
    var link = createLink.apply(Command.CreateLink.KEY, "https://skillfactory.ru/", "1");
    LinkDTO redirectedLink = verifyLinkIsVisitedWhileCalling(
        "https://skillfactory.ru/",
        () -> redirect.apply(Command.Redirect.KEY, Cli.SHORTENED_URL_PREFIX + link.code())
    );
    assertThat(redirectedLink.originalUrl()).isEqualTo("https://skillfactory.ru/");
    assertThat(redirectedLink.redirectLimit()).isZero();

    verifyNoLinkIsVisitedWhileCalling(
        () -> redirect.apply(Command.Redirect.KEY, Cli.SHORTENED_URL_PREFIX + link.code()));

    assertThat(listActiveLinks.apply(Command.ListActiveLinks.KEY)).isEmpty();

    TestLogger redirectTestLogger = TestLoggerFactory.getTestLogger(Command.Redirect.class);

    assertThat(redirectTestLogger.getAllLoggingEvents())
        .contains(LoggingEvent.info(
            Command.Redirect.INVALID_LINK_LOG_MESSAGE_PATTERN,
            LinkValidationService.REDIRECT_LIMIT_REACHED
        ));
  }

  /**
   * 3. Тестируйте удаление ссылок по истечении срока жизни. 4.2 Убедитесь, что пользователи
   * получают уведомления о том, что их ссылка недоступна из-за истечения срока жизни
   */
  @Test
  void check_link_ttl() throws IOException {
    initContextWithProperties("short_ttl_test.properties"); // ttl = 1ms
    initCommandBeans();

    var link = createLink.apply(Command.CreateLink.KEY, "https://skillfactory.ru/", "1");

    var attemptedLink = verifyNoLinkIsVisitedWhileCalling(
        () -> redirect.apply(Command.Redirect.KEY, Cli.SHORTENED_URL_PREFIX + link.code())
    );
    assertThat(attemptedLink.originalUrl()).isEqualTo("https://skillfactory.ru/");
    assertThat(attemptedLink.redirectLimit()).isOne();
    assertThat(attemptedLink.status()).isEqualTo(LinkStatus.INVALID.name());

    TestLogger redirectTestLogger = TestLoggerFactory.getTestLogger(Command.Redirect.class);

    assertThat(redirectTestLogger.getAllLoggingEvents())
        .contains(LoggingEvent.info(
            Command.Redirect.INVALID_LINK_LOG_MESSAGE_PATTERN,
            LinkValidationService.EXPIRED
        ));
  }

  @Test
  void check_full_scenario() {
    LinkDTO createdLink = createLink.apply(Command.CreateLink.KEY, "https://skillfactory.ru/",
        "100");
    assertThat(createdLink).isNotNull();

    String newUrl = "https://ya.ru";
    Optional<LinkDTO> linkWithNewUrl = editLinkUrl.apply(
        Command.EditLinkUrl.KEY,
        Cli.SHORTENED_URL_PREFIX + createdLink.code(),
        newUrl
    );
    assertThat(linkWithNewUrl).get().extracting(LinkDTO::originalUrl).isEqualTo(newUrl);

    int newRedirectLimit = 10;
    Optional<LinkDTO> linkWithNewRedirectLimit = editLinkRedirectLimit.apply(
        Command.EditLinkUrl.KEY,
        Cli.SHORTENED_URL_PREFIX + createdLink.code(),
        String.valueOf(newRedirectLimit)
    );
    assertThat(linkWithNewRedirectLimit).isNotEmpty();

    assertThat(linkWithNewRedirectLimit).get()
        .extracting(LinkDTO::redirectLimit, InstanceOfAssertFactories.INTEGER)
        .isEqualTo(newRedirectLimit);

    LinkDTO redirectedLink = verifyLinkIsVisitedWhileCalling(
        newUrl,
        () -> redirect.apply(Command.Redirect.KEY, Cli.SHORTENED_URL_PREFIX + createdLink.code())
    );

    assertThat(redirectedLink).isNotNull();

    assertThat(redirectedLink)
        .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("redirectLimit")
            .build())
        .isEqualTo(linkWithNewRedirectLimit.get());

    assertThat(redirectedLink.redirectLimit()).isEqualTo(
        linkWithNewRedirectLimit.get().redirectLimit() - 1);

    List<LinkDTO> activeLinks = listActiveLinks.apply(Command.ListActiveLinks.KEY);

    assertThat(activeLinks)
        .containsExactly(redirectedLink);

    String user1 = Cli.USER_UUID.get();

    // emulate new session
    Cli.USER_UUID.remove();

    createLink.apply(Command.CreateLink.KEY, "https://google.com/", "100");

    assertThat(listActiveLinks.apply(Command.ListActiveLinks.KEY))
        .extracting(LinkDTO::originalUrl)
        .containsExactly("https://google.com/");

    assertThatCode(() -> login.apply(Command.Login.KEY, user1))
        .doesNotThrowAnyException();

    assertThat(listActiveLinks.apply(Command.ListActiveLinks.KEY))
        .containsExactly(redirectedLink);

  }

  private <T> T verifyLinkIsVisitedWhileCalling(String link, Callable<T> visitingCallback) {
    try (MockedStatic<Desktop> mockedDesktop = Mockito.mockStatic(Desktop.class)) {
      var desktopMock = Mockito.mock(Desktop.class);
      mockedDesktop.when(Desktop::getDesktop).thenReturn(desktopMock);
      Mockito.doNothing().when(desktopMock).browse(Mockito.any(URI.class));

      T result = visitingCallback.call();

      Mockito.verify(desktopMock).browse(uriArgumentCaptor.capture());
      assertThat(uriArgumentCaptor.getValue().toString()).isEqualTo(link);

      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T verifyNoLinkIsVisitedWhileCalling(Callable<T> visitingCallback) {
    try (MockedStatic<Desktop> mockedDesktop = Mockito.mockStatic(Desktop.class)) {
      var desktopMock = Mockito.mock(Desktop.class);
      mockedDesktop.when(Desktop::getDesktop).thenReturn(desktopMock);
      Mockito.doNothing().when(desktopMock).browse(Mockito.any(URI.class));

      T result = visitingCallback.call();

      Mockito.verifyNoInteractions(desktopMock);
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void initContextWithProperties(String path) throws IOException {
    ctx = new AnnotationConfigApplicationContext();
    ctx.register(TestAppConfig.class);
    PropertiesUtil.addApplicationProperties(ctx, path);
    ctx.refresh();
  }

  private void initCommandBeans() {
    createLink = ctx.getBean(Command.CreateLink.class);
    redirect = ctx.getBean(Command.Redirect.class);
    listActiveLinks = ctx.getBean(Command.ListActiveLinks.class);
    editLinkUrl = ctx.getBean(Command.EditLinkUrl.class);
    editLinkRedirectLimit = ctx.getBean(Command.EditLinkRedirectLimit.class);
    login = ctx.getBean(Command.Login.class);
  }
}
