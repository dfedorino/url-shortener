package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class Login implements Command<String> {

    public static final String KEY_TOKEN = "login";
    public static final String SUCCESS_MESSAGE = "Login successful!";

    private final UserService userService;

    @Override
    public ResultWithNotification<String> apply(String... commandAndArgs) {
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 2, commandAndArgs),
                () -> validateUuid(commandAndArgs[1])
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        String uuid = commandAndArgs[1];

        if (userService.find(uuid).isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.USER_NOT_FOUND_MESSAGE.formatted(uuid));
        }

        Cli.USER_UUID.set(uuid);
        return ResultWithNotification.ofPayload(SUCCESS_MESSAGE, uuid);
    }

    @Override
    public String key() {
        return KEY_TOKEN;
    }

    @Override
    public String description() {
        return "Login with UUID";
    }

    @Override
    public String example() {
        return KEY_TOKEN + " UUID";
    }
}
