package com.dfedorino.urlshortener.ui.console.command.dto;

import java.util.Optional;

public record ResultWithNotification<T>(
        String notification,
        Optional<T> result
) {

    public static <T> ResultWithNotification<T> ofErrorMessage(String notification) {
        return new ResultWithNotification<>(notification, Optional.empty());
    }

    public static <T> ResultWithNotification<T> ofPayload(String notification, T payload) {
        return new ResultWithNotification<>(notification, Optional.ofNullable(payload));
    }
}
