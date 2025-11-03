package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ListActiveLinks implements Command<List<LinkDto>> {

    public static final String KEY_TOKEN = "list";
    public static final String SUCCESS_MESSAGE = "Active link successfully fetched!";
    public static final String DESCRIPTION_MESSAGE = "List active created links";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public ResultWithNotification<List<LinkDto>> apply(String... commandAndArgs) {
        var argsInvalidCheck = validateArguments(args -> args.length == 1, commandAndArgs);

        if (argsInvalidCheck.isPresent()) {
            return argsInvalidCheck.get();
        }

        Optional<User> user = userService.find(Cli.USER_UUID.get());
        if (user.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.USER_NOT_FOUND_MESSAGE.formatted(Cli.USER_UUID.get()));
        }

        List<LinkDto> validUserLinks = linkService.findValidUserLinks(user.get().getId());
        return ResultWithNotification.ofPayload(SUCCESS_MESSAGE, validUserLinks);
    }

    @Override
    public String key() {
        return KEY_TOKEN;
    }

    @Override
    public String description() {
        return DESCRIPTION_MESSAGE;
    }

    @Override
    public String example() {
        return KEY_TOKEN;
    }
}
