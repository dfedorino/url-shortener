package com.dfedorino.urlshortener.ui.console.command;

import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import com.dfedorino.urlshortener.ui.console.util.ValidationUtil;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a CLI command that can be executed with arguments.
 *
 * <p>Provides common validation helpers for argument checking, limits, URLs, UUIDs,
 * and short links common for all commands. Implementations of this interface should define the
 * command key, description, example usage, and the logic of applying the given arguments.
 *
 * @param <T> the type of the result returned by the command
 */
public interface Command<T> {

    String COMMAND_INVALID_MESSAGE = "Command invalid, example: '%s'";
    String LIMIT_INVALID_MESSAGE = "Limit invalid: '%s'";
    String SHORT_LINK_INVALID_MESSAGE = "Short link invalid: '%s'";
    String SHORT_LINK_DELETED_MESSAGE = "Short link deleted: '%s'";
    String URL_INVALID_MESSAGE = "URL invalid: '%s'";
    String USER_NOT_FOUND_MESSAGE = "User not found: '%s'";
    String SHORT_LINK_NOT_FOUND_MESSAGE = "Link not found: '%s'";
    String UUID_INVALID_MESSAGE = "Invalid UUID: %s";

    /**
     * Executes the command with the given arguments.
     *
     * @param args the command arguments
     * @return the result of execution wrapped in {@link ResultWithNotification}
     */
    ResultWithNotification<T> apply(String... args);

    /**
     * Returns the unique key or name of the command.
     *
     * @return the command key
     */
    String key();

    /**
     * Returns a description of what the command does.
     *
     * @return the command description
     */
    String description();

    /**
     * Returns an example usage of the command.
     *
     * @return example usage
     */
    String example();

    /**
     * Validates command arguments using the given condition, typically for length check.
     *
     * @param condition      predicate to test the arguments
     * @param commandAndArgs the arguments to validate
     * @return an {@link Optional} containing an error result if validation fails, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateArguments(Predicate<String[]> condition,
                                                                  String[] commandAndArgs) {
        if (!condition.test(commandAndArgs)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.COMMAND_INVALID_MESSAGE.formatted(example())));
        }
        return Optional.empty();
    }

    /**
     * Validates that a given short link is well-formed.
     *
     * @param shortLink the short link string to validate
     * @return an {@link Optional} containing an error result if invalid, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateShortLink(String shortLink) {
        if (!ValidationUtil.isValidShortLink(shortLink)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.SHORT_LINK_INVALID_MESSAGE.formatted(shortLink)));
        }
        return Optional.empty();
    }

    /**
     * Validates that a given limit is valid.
     *
     * @param limit the limit string to validate
     * @return an {@link Optional} containing an error result if invalid, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateLimit(String limit) {
        if (!ValidationUtil.isValidLimit(limit)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.LIMIT_INVALID_MESSAGE.formatted(limit)));
        }
        return Optional.empty();
    }

    /**
     * Validates that a given original URL is a valid URL.
     *
     * @param originalUrl the URL string to validate
     * @return an {@link Optional} containing an error result if invalid, empty otherwise
     */
    default Optional<ResultWithNotification<T>> validateOriginalUrl(String originalUrl) {
        if (!ValidationUtil.isValidUrl(originalUrl)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.URL_INVALID_MESSAGE.formatted(originalUrl)));
        }
        return Optional.empty();
    }

    /**
     * Validates that a given UUID is a valid UUID.
     *
     * @param uuid the UUID string to validate
     * @return an {@link Optional} containing an error result if invalid, empty otherwise
     */
    default Optional<ResultWithNotification<String>> validateUuid(String uuid) {
        if (!ValidationUtil.isValidUuid(uuid)) {
            return Optional.of(ResultWithNotification.ofErrorMessage(
                    Command.UUID_INVALID_MESSAGE.formatted(uuid)));
        }
        return Optional.empty();
    }

    /**
     * Performs multiple validations in order and returns the first failing result, if any.
     *
     * @param validations a list of suppliers for individual validations
     * @return the first failing validation result, or empty if all validations pass
     */
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
