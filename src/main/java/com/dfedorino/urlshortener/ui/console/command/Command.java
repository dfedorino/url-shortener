package com.dfedorino.urlshortener.ui.console.command;

import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import com.dfedorino.urlshortener.ui.console.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Command<T> {

    String COMMAND_INVALID_MESSAGE = "Command invalid, example: '%s'";
    String LIMIT_INVALID_MESSAGE = "Limit invalid: '%s'";
    String SHORT_LINK_INVALID_MESSAGE = "Short link invalid: '%s'";
    String URL_INVALID_MESSAGE = "URL invalid: '%s'";
    String USER_NOT_FOUND_MESSAGE = "User not found: '%s'";
    String SHORT_LINK_NOT_FOUND_MESSAGE = "Link not found: '%s'";
    String UUID_INVALID_MESSAGE = "Invalid UUID: %s";

    ResultWithNotification<T> apply(String... args);

    String key();

    String description();

    String example();

    default Optional<ResultWithNotification<T>> validateArguments(Predicate<String[]> condition,
                                                                  String[] commandAndArgs) {
        if (!condition.test(commandAndArgs)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.COMMAND_INVALID_MESSAGE.formatted(example())));
        }
        return Optional.empty();
    }

    default Optional<ResultWithNotification<T>> validateShortLink(String shortLink) {
        if (!ValidationUtil.isValidShortLink(shortLink)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.SHORT_LINK_INVALID_MESSAGE.formatted(shortLink)));
        }
        return Optional.empty();
    }

    default Optional<ResultWithNotification<T>> validateLimit(String limit) {
        if (!ValidationUtil.isValidLimit(limit)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.LIMIT_INVALID_MESSAGE.formatted(limit)));
        }
        return Optional.empty();
    }

    default Optional<ResultWithNotification<T>> validateOriginalUrl(String originalUrl) {
        if (!ValidationUtil.isValidUrl(originalUrl)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.URL_INVALID_MESSAGE.formatted(originalUrl)));
        }
        return Optional.empty();
    }

    default Optional<ResultWithNotification<String>> validateUuid(String uuid) {
        if (!ValidationUtil.isValidUuid(uuid)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.UUID_INVALID_MESSAGE.formatted(uuid)));
        }
        return Optional.empty();
    }

    default Optional<ResultWithNotification<T>> validateInOrder(
            List<Supplier<Optional<ResultWithNotification<T>>>> validations) {

        for (var validation : validations) {
            Optional<ResultWithNotification<T>> failedValidation = validation.get();
            if (failedValidation.isPresent()) {
                return failedValidation;
            }
        }
        return Optional.empty();
    }
}
