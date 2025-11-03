package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;

public final class Quit implements Command<Void> {

    public static final String KEY_TOKEN = "quit";
    public static final String DESCRIPTION_MESSAGE = "Exit";

    @Override
    public ResultWithNotification<Void> apply(String... commandAndArgs) {
        var failedArgsCheck = validateArguments(args -> args.length == 1, commandAndArgs);
        if (failedArgsCheck.isPresent()) {
            return failedArgsCheck.get();
        }
        System.exit(0);
        return null;
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
