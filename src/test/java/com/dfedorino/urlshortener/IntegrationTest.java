package com.dfedorino.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.jdbc.util.DataUtil;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.impl.CreateLink;
import com.dfedorino.urlshortener.ui.console.command.impl.DeleteLink;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkRedirectLimit;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkUrl;
import com.dfedorino.urlshortener.ui.console.command.impl.ListActiveLinks;
import com.dfedorino.urlshortener.ui.console.command.impl.Login;
import com.dfedorino.urlshortener.ui.console.command.impl.Redirect;
import com.dfedorino.urlshortener.util.DatabaseUtil;
import com.dfedorino.urlshortener.util.PropertiesUtil;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
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

    private CreateLink createLink;
    private Redirect redirect;
    private ListActiveLinks listActiveLinks;
    private EditLinkUrl editLinkUrl;
    private EditLinkRedirectLimit editLinkRedirectLimit;
    private Login login;
    private DeleteLink deleteLink;

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
        LinkDto link1 = givenLinkIsCreated("https://skillfactory.ru/", "100");
        String user1 = Cli.USER_UUID.get();

        // emulate new session
        Cli.USER_UUID.remove();

        LinkDto link2 = givenLinkIsCreated("https://skillfactory.ru/", "100");
        String user2 = Cli.USER_UUID.get();

        assertThat(user1).isNotEqualTo(user2);
        assertThat(link1.originalUrl()).isEqualTo(link2.originalUrl());
        assertThat(link1.code()).isNotEqualTo(link2.code());
    }

    /**
     * 2. Проверьте, что при исчерпании лимита переходов переход по ссылке блокируется. 4.1
     * Убедитесь, что пользователи получают уведомления о том, что их ссылка недоступна из-за
     * исчерпания лимита
     */
    @Test
    void check_redirect_limit() {
        LinkDto link = givenLinkIsCreated("https://skillfactory.ru/", "1");
        String shortLink = Cli.SHORTENED_URL_PREFIX + link.code();

        LinkDto redirectedLink = verifyLinkIsVisitedWhileCalling(
                "https://skillfactory.ru/",
                () -> givenLinkRedirected(shortLink)
        );

        assertThat(redirectedLink.originalUrl()).isEqualTo("https://skillfactory.ru/");
        assertThat(redirectedLink.redirectLimit()).isZero();

        verifyNoLinkIsVisitedWhileCalling(
                () -> givenLinkRedirectionFailed(shortLink,
                                                 LinkValidationService.REDIRECT_LIMIT_REACHED));

        List<LinkDto> activeLinks = givenActiveLinksListed();
        assertThat(activeLinks).isEmpty();
    }

    /**
     * 3. Тестируйте удаление ссылок по истечении срока жизни. 4.2 Убедитесь, что пользователи
     * получают уведомления о том, что их ссылка недоступна из-за истечения срока жизни
     */
    @Test
    void check_link_ttl() throws IOException {
        initContextWithProperties("short_ttl_test.properties"); // ttl = 1ms
        initCommandBeans();

        var createdLink = givenLinkIsCreated("https://skillfactory.ru/", "1");

        String shortLink = Cli.SHORTENED_URL_PREFIX + createdLink.code();
        var staleLink = verifyNoLinkIsVisitedWhileCalling(
                () -> givenLinkRedirectionFailed(shortLink, LinkValidationService.EXPIRED)
        );
        assertThat(staleLink.originalUrl()).isEqualTo("https://skillfactory.ru/");
        assertThat(staleLink.redirectLimit()).isOne();
        assertThat(staleLink.status()).isEqualTo(LinkStatus.INVALID.name());
    }

    @Test
    void check_full_scenario() {
        LinkDto createdLink = givenLinkIsCreated("https://skillfactory.ru/", "100");
        String shortLink = Cli.SHORTENED_URL_PREFIX + createdLink.code();

        String newUrl = "https://ya.ru";
        LinkDto linkWithNewUrl = givenLinkUrlUpdated(shortLink, newUrl);
        assertThat(linkWithNewUrl.originalUrl()).isEqualTo(newUrl);

        int newRedirectLimit = 10;
        LinkDto linkWithNewRedirectLimit = givenLinkRedirectLimitUpdated(shortLink,
                                                                         "" + newRedirectLimit);
        assertThat(linkWithNewRedirectLimit.redirectLimit())
                .isEqualTo(newRedirectLimit);

        LinkDto redirectedLink = verifyLinkIsVisitedWhileCalling(
                newUrl,
                () -> givenLinkRedirected(shortLink)
        );

        assertThat(redirectedLink)
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                  .withIgnoredFields("redirectLimit")
                                                  .build())
                .isEqualTo(linkWithNewRedirectLimit);

        assertThat(redirectedLink.redirectLimit()).isEqualTo(
                linkWithNewRedirectLimit.redirectLimit() - 1);

        List<LinkDto> activeLinks = givenActiveLinksListed();

        assertThat(activeLinks)
                .containsExactly(redirectedLink);

        String user1 = Cli.USER_UUID.get();

        // emulate new session
        Cli.USER_UUID.remove();

        givenLinkIsCreated("https://google.com/", "100");

        assertThat(givenActiveLinksListed())
                .extracting(LinkDto::originalUrl)
                .containsExactly("https://google.com/");

        assertThatCode(() -> givenLoginPerformed(user1))
                .doesNotThrowAnyException();

        assertThat(givenActiveLinksListed())
                .containsExactly(redirectedLink);

        LinkDto deletedLink = givenLinkDeleted(shortLink);
        assertThat(deletedLink.status()).isEqualTo(LinkStatus.DELETED.name());
        assertThat(givenActiveLinksListed())
                .isEmpty();
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
        createLink = ctx.getBean(CreateLink.class);
        redirect = ctx.getBean(Redirect.class);
        listActiveLinks = ctx.getBean(ListActiveLinks.class);
        editLinkUrl = ctx.getBean(EditLinkUrl.class);
        editLinkRedirectLimit = ctx.getBean(EditLinkRedirectLimit.class);
        login = ctx.getBean(Login.class);
        deleteLink = ctx.getBean(DeleteLink.class);
    }

    private LinkDto givenLinkIsCreated(String url, String redirectLimit) {
        var createLinkResult = createLink.apply(CreateLink.KEY_TOKEN,
                                                url,
                                                redirectLimit);

        assertThat(createLinkResult.notification()).isEqualTo(CreateLink.SUCCESS_MESSAGE);
        assertThat(createLinkResult.result()).isNotEmpty();
        return createLinkResult.result().get();
    }

    private LinkDto givenLinkRedirected(String shortLink) {
        var redirectedLinkResult = redirect.apply(Redirect.KEY_TOKEN, shortLink);

        assertThat(redirectedLinkResult.notification()).isEqualTo(Redirect.SUCCESS_MESSAGE);
        assertThat(redirectedLinkResult.result()).isNotEmpty();

        return redirectedLinkResult.result().get();
    }

    private LinkDto givenLinkRedirectionFailed(String shortLink, String reason) {
        var redirectedLinkResult = redirect.apply(Redirect.KEY_TOKEN, shortLink);

        assertThat(redirectedLinkResult.notification()).isEqualTo(reason);
        assertThat(redirectedLinkResult.result()).isNotEmpty();

        return redirectedLinkResult.result().get();
    }

    private List<LinkDto> givenActiveLinksListed() {
        var listActiveLinksResult = listActiveLinks.apply(ListActiveLinks.KEY_TOKEN);

        assertThat(listActiveLinksResult.notification()).isEqualTo(ListActiveLinks.SUCCESS_MESSAGE);
        assertThat(listActiveLinksResult.result()).isNotEmpty();

        return listActiveLinksResult.result().get();
    }

    private LinkDto givenLinkUrlUpdated(String shortLink, String newUrl) {
        var editLinkUrlResult = editLinkUrl.apply(EditLinkUrl.KEY_TOKEN, shortLink, newUrl);

        assertThat(editLinkUrlResult.notification()).isEqualTo(EditLinkUrl.SUCCESS_MESSAGE);
        assertThat(editLinkUrlResult.result()).isNotEmpty();

        return editLinkUrlResult.result().get();
    }

    private LinkDto givenLinkRedirectLimitUpdated(String shortLink, String newRedirectLimit) {
        var editLinkRedirectLimitResult = editLinkRedirectLimit.apply(EditLinkRedirectLimit.KEY_TOKEN,
                                                                      shortLink,
                                                                      newRedirectLimit);

        assertThat(editLinkRedirectLimitResult.notification())
                .isEqualTo(EditLinkRedirectLimit.SUCCESS_MESSAGE);
        assertThat(editLinkRedirectLimitResult.result()).isNotEmpty();

        return editLinkRedirectLimitResult.result().get();
    }

    private void givenLoginPerformed(String uuid) {
        var loginResult = login.apply(Login.KEY_TOKEN, uuid);

        assertThat(loginResult.notification())
                .isEqualTo(Login.SUCCESS_MESSAGE);
        assertThat(loginResult.result()).isNotEmpty();
    }

    private LinkDto givenLinkDeleted(String shortLink) {
        var deleteLinkResult = deleteLink.apply(DeleteLink.KEY_TOKEN, shortLink);

        assertThat(deleteLinkResult.notification())
                .isEqualTo(DeleteLink.SUCCESS_MESSAGE);
        assertThat(deleteLinkResult.result()).isNotEmpty();

        return deleteLinkResult.result().get();
    }

}
