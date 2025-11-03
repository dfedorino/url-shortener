package com.dfedorino.urlshortener.ui.console.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import com.dfedorino.urlshortener.TestConstants;
import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import com.dfedorino.urlshortener.ui.console.command.impl.CreateLink;
import com.dfedorino.urlshortener.ui.console.command.impl.DeleteLink;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkRedirectLimit;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkUrl;
import com.dfedorino.urlshortener.ui.console.command.impl.ListActiveLinks;
import com.dfedorino.urlshortener.ui.console.command.impl.Login;
import com.dfedorino.urlshortener.ui.console.command.impl.Quit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CommandTest {

    private static final UserService MOCK_USER_SERVICE = mock(UserService.class);
    private static final LinkService MOCK_LINK_SERVICE = mock(LinkService.class);
    private static final Command<LinkDto> CREATE_LINK = new CreateLink(
            MOCK_USER_SERVICE,
            MOCK_LINK_SERVICE
    );
    private static final Command<LinkDto> DELETE_LINK = new DeleteLink(
            MOCK_USER_SERVICE,
            MOCK_LINK_SERVICE
    );
    private static final Command<LinkDto> EDIT_LINK_REDIRECT_LIMIT = new EditLinkRedirectLimit(
            MOCK_USER_SERVICE,
            MOCK_LINK_SERVICE
    );
    private static final Command<LinkDto> EDIT_LINK_URL = new EditLinkUrl(
            MOCK_USER_SERVICE,
            MOCK_LINK_SERVICE
    );
    private static final Command<List<LinkDto>> LIST_ACTIVE_LINKS = new ListActiveLinks(
            MOCK_USER_SERVICE,
            MOCK_LINK_SERVICE
    );
    private static final Command<String> LOGIN = new Login(
            MOCK_USER_SERVICE
    );
    private static final Command<Void> QUIT = new Quit();

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    private static List<Arguments> commandsAndMetadata() {
        return List.of(
                Arguments.of(CreateLink.KEY_TOKEN,
                             CreateLink.DESCRIPTION_MESSAGE,
                             CreateLink.EXAMPLE_MESSAGE,
                             CREATE_LINK),

                Arguments.of(DeleteLink.KEY_TOKEN,
                             DeleteLink.DESCRIPTION_MESSAGE,
                             DeleteLink.EXAMPLE_MESSAGE,
                             DELETE_LINK),

                Arguments.of(EditLinkRedirectLimit.KEY_TOKEN,
                             EditLinkRedirectLimit.DESCRIPTION_MESSAGE,
                             EditLinkRedirectLimit.EXAMPLE_MESSAGE,
                             EDIT_LINK_REDIRECT_LIMIT),

                Arguments.of(EditLinkUrl.KEY_TOKEN,
                             EditLinkUrl.DESCRIPTION_MESSAGE,
                             EditLinkUrl.EXAMPLE_MESSAGE,
                             EDIT_LINK_URL),

                Arguments.of(ListActiveLinks.KEY_TOKEN,
                             ListActiveLinks.DESCRIPTION_MESSAGE,
                             ListActiveLinks.KEY_TOKEN,
                             LIST_ACTIVE_LINKS),

                Arguments.of(Login.KEY_TOKEN,
                             Login.DESCRIPTION_MESSAGE,
                             Login.EXAMPLE_MESSAGE,
                             LOGIN),

                Arguments.of(Quit.KEY_TOKEN,
                             Quit.DESCRIPTION_MESSAGE,
                             Quit.KEY_TOKEN,
                             QUIT)
        );
    }

    private static List<Arguments> commandsAndArgs() {
        return List.of(
                Arguments.of(CreateLink.KEY_TOKEN,
                             new String[]{
                                     CreateLink.KEY_TOKEN,
                                     TestConstants.VALID_URL,
                                     String.valueOf(TestConstants.REDIRECT_LIMIT)
                             },
                             CREATE_LINK),

                Arguments.of(DeleteLink.KEY_TOKEN,
                             new String[]{DeleteLink.KEY_TOKEN, TestConstants.SHORT_LINK},
                             DELETE_LINK),

                Arguments.of(EditLinkRedirectLimit.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.SHORT_LINK,
                                     TestConstants.NEW_REDIRECT_LIMIT
                             },
                             EDIT_LINK_REDIRECT_LIMIT),

                Arguments.of(EditLinkUrl.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.SHORT_LINK,
                                     TestConstants.NEW_URL
                             },
                             EDIT_LINK_URL),

                Arguments.of(ListActiveLinks.KEY_TOKEN,
                             new String[]{ListActiveLinks.KEY_TOKEN},
                             LIST_ACTIVE_LINKS),

                Arguments.of(Login.KEY_TOKEN,
                             new String[]{Login.KEY_TOKEN, TestConstants.USER_UUID},
                             LOGIN),

                Arguments.of(Quit.KEY_TOKEN,
                             new String[]{Quit.KEY_TOKEN},
                             QUIT)
        );
    }

    private static List<Arguments> commandsWithInvalidUrls() {
        return List.of(
                Arguments.of(CreateLink.KEY_TOKEN,
                             new String[]{
                                     CreateLink.KEY_TOKEN,
                                     TestConstants.INVALID_URL,
                                     String.valueOf(TestConstants.REDIRECT_LIMIT)
                             },
                             CREATE_LINK),

                Arguments.of(EditLinkUrl.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.SHORT_LINK,
                                     TestConstants.INVALID_URL
                             },
                             EDIT_LINK_URL)
        );
    }

    private static List<Arguments> commandsWithInvalidShortLinks() {
        return List.of(
                Arguments.of(DeleteLink.KEY_TOKEN,
                             new String[]{DeleteLink.KEY_TOKEN, TestConstants.INVALID_URL},
                             DELETE_LINK),

                Arguments.of(EditLinkRedirectLimit.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.INVALID_URL,
                                     TestConstants.NEW_REDIRECT_LIMIT
                             },
                             EDIT_LINK_REDIRECT_LIMIT),

                Arguments.of(EditLinkUrl.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.INVALID_URL,
                                     TestConstants.NEW_URL
                             },
                             EDIT_LINK_URL)
        );
    }

    private static List<Arguments> commandsWithInvalidRedirectLimits() {
        return List.of(
                Arguments.of(CreateLink.KEY_TOKEN,
                             new String[]{
                                     CreateLink.KEY_TOKEN,
                                     TestConstants.VALID_URL,
                                     String.valueOf(TestConstants.INVALID_REDIRECT_LIMIT)
                             },
                             CREATE_LINK),

                Arguments.of(EditLinkRedirectLimit.KEY_TOKEN,
                             new String[]{
                                     EditLinkRedirectLimit.KEY_TOKEN,
                                     TestConstants.SHORT_LINK,
                                     String.valueOf(TestConstants.INVALID_REDIRECT_LIMIT)
                             },
                             EDIT_LINK_REDIRECT_LIMIT)
        );
    }

    @BeforeEach
    void setUp() {
        clearInvocations(MOCK_USER_SERVICE, MOCK_LINK_SERVICE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsAndMetadata")
    void check_common_methods(String key, String description, String example, Command<?> impl) {
        assertThat(impl.key()).isEqualTo(key);
        assertThat(impl.description()).isEqualTo(description);
        assertThat(impl.example()).isEqualTo(example);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsAndArgs")
    void check_args_validation(String key, String[] args, Command<?> impl) {
        String[] invalidArgs = new String[args.length + 1];
        args[0] = key;
        Arrays.fill(invalidArgs, 1, invalidArgs.length, "arg");

        var result = impl.apply(invalidArgs);

        log.info(">> {}", result.notification());

        assertThat(result.notification())
                .isEqualTo(Command.COMMAND_INVALID_MESSAGE.formatted(impl.example()));

        verifyNoInteractions(MOCK_USER_SERVICE, MOCK_LINK_SERVICE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsWithInvalidUrls")
    void check_url_validation(String key, String[] args, Command<?> impl) {
        ResultWithNotification<?> result = impl.apply(args);

        log.info(">> {}", result.notification());

        assertThat(result.notification())
                .isEqualTo(
                        Command.URL_INVALID_MESSAGE.formatted(TestConstants.INVALID_URL));

        verifyNoInteractions(MOCK_USER_SERVICE, MOCK_LINK_SERVICE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsWithInvalidShortLinks")
    void check_short_link_validation(String key, String[] args, Command<?> impl) {
        ResultWithNotification<?> result = impl.apply(args);

        log.info(">> {}", result.notification());

        assertThat(result.notification())
                .isEqualTo(
                        Command.SHORT_LINK_INVALID_MESSAGE.formatted(TestConstants.INVALID_URL));

        verifyNoInteractions(MOCK_USER_SERVICE, MOCK_LINK_SERVICE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsWithInvalidRedirectLimits")
    void check_redirect_limit_validation(String key, String[] args, Command<?> impl) {
        ResultWithNotification<?> result = impl.apply(args);

        log.info(">> {}", result.notification());

        assertThat(result.notification())
                .isEqualTo(Command.LIMIT_INVALID_MESSAGE.formatted(
                        TestConstants.INVALID_REDIRECT_LIMIT));

        verifyNoInteractions(MOCK_USER_SERVICE, MOCK_LINK_SERVICE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandsAndArgs")
    void check_user_validation(String key, String[] args, Command<?> impl) {
        if (CreateLink.KEY_TOKEN.equals(key) || Quit.KEY_TOKEN.equals(key)) {
            return;
        }

        given(MOCK_USER_SERVICE.find(anyString()))
                .willReturn(Optional.empty());

        ResultWithNotification<?> result = impl.apply(args);

        verify(MOCK_USER_SERVICE).find(stringArgumentCaptor.capture());

        assertThat(result.notification())
                .isEqualTo(
                        Command.USER_NOT_FOUND_MESSAGE.formatted(stringArgumentCaptor.getValue()));

        verifyNoInteractions(MOCK_LINK_SERVICE);


    }
}