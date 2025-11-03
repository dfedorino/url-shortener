package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CreateLink implements Command<LinkDto> {

    public static final String KEY_TOKEN = "create";
    public static final String EXAMPLE_MESSAGE = KEY_TOKEN + " https://skillfactory.ru/ 100";
    public static final String SUCCESS_MESSAGE = "Link successfully created!";
    public static final String DESCRIPTION_MESSAGE = "Shorten the given URL";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public ResultWithNotification<LinkDto> apply(String... commandAndArgs) {
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 3, commandAndArgs),
                () -> validateOriginalUrl(commandAndArgs[1]),
                () -> validateLimit(commandAndArgs[2])
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        Long userId = userService.find(Cli.USER_UUID.get())
                .orElseGet(() -> userService.create(Cli.USER_UUID.get())).getId();

        LinkDto link = linkService.createLink(userId,
                                              commandAndArgs[1],
                                              Integer.parseInt(commandAndArgs[2]));

        return ResultWithNotification.ofPayload(SUCCESS_MESSAGE, link);
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
        return EXAMPLE_MESSAGE;
    }
}
