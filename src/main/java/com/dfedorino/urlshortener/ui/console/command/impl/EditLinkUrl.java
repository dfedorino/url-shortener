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
public final class EditLinkUrl implements Command<LinkDto> {

    public static final String KEY_TOKEN = "edit_url";
    public static final String SUCCESS_MESSAGE = "URL successfully changed!";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public ResultWithNotification<LinkDto> apply(String... commandAndArgs) {
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 3, commandAndArgs),
                () -> validateShortLink(commandAndArgs[1]),
                () -> validateOriginalUrl(commandAndArgs[2])
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        String link = commandAndArgs[1];
        String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());
        String newUrl = commandAndArgs[2];

        Optional<User> user = userService.find(Cli.USER_UUID.get());
        if (user.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.USER_NOT_FOUND_MESSAGE.formatted(Cli.USER_UUID.get()));
        }

        Optional<LinkDto> editedLink = linkService.updateOriginalUrl(user.get().getId(),
                                                                     linkCode,
                                                                     newUrl);
        return new ResultWithNotification<>(SUCCESS_MESSAGE, editedLink);
    }

    @Override
    public String key() {
        return KEY_TOKEN;
    }

    @Override
    public String description() {
        return "Edit active link URL";
    }

    @Override
    public String example() {
        return KEY_TOKEN + Cli.SHORTENED_URL_PREFIX + "AQA -url https://skillfactory.ru";
    }
}
