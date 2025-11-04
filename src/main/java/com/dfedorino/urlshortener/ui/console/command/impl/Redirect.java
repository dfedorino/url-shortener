package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService.ValidatedLink;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Redirect implements Command<LinkDto> {

    public static final String KEY_TOKEN = "redirect";
    public static final String EXAMPLE_MESSAGE = KEY_TOKEN + " " + Cli.SHORTENED_URL_PREFIX + "AQ";
    public static final String SUCCESS_MESSAGE = "Successfully redirected!";
    public static final String FAILED_TO_BROWSE_LINK = "Failed to browse link: %s";
    public static final String DESCRIPTION_MESSAGE = "Redirect with the given short URL to the original URL";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public ResultWithNotification<LinkDto> apply(String... commandAndArgs) {
        var firstFailedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 2, commandAndArgs),
                () -> validateShortLink(commandAndArgs[1])
        ));

        if (firstFailedCheck.isPresent()) {
            return firstFailedCheck.get();
        }

        String link = commandAndArgs[1];
        String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());

        Optional<User> user = userService.find(Cli.USER_UUID.get());

        if (user.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.USER_NOT_FOUND_MESSAGE.formatted(Cli.USER_UUID.get()));
        }

        var optionalValidatedLink = linkService.findValidatedLink(user.get().getId(),
                                                                  linkCode);

        if (optionalValidatedLink.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.SHORT_LINK_NOT_FOUND_MESSAGE.formatted(link));
        }

        ValidatedLink validatedLink = optionalValidatedLink.get();

        if (LinkStatus.DELETED.name().equals(validatedLink.link().status())) {
            return ResultWithNotification.ofErrorMessage(
                    Command.SHORT_LINK_DELETED_MESSAGE.formatted(link));
        }

        if (validatedLink.status() == LinkValidationService.Status.INVALID) {
            Optional<LinkDto> invalidLink = linkService.invalidateLink(
                    validatedLink.link().userId(),
                    validatedLink.link().code()
            );
            return new ResultWithNotification<>(
                    validatedLink.reasonWhyInvalid(),
                    invalidLink
            );
        }

        try {
            Desktop.getDesktop().browse(URI.create(validatedLink.link().originalUrl()));
            int newRedirectLimit = validatedLink.link().redirectLimit() - 1;
            Optional<LinkDto> updatedLink = linkService.updateRedirectLimit(user.get().getId(),
                                                                            linkCode,
                                                                            newRedirectLimit);
            return new ResultWithNotification<>(SUCCESS_MESSAGE, updatedLink);
        } catch (IOException e) {
            return ResultWithNotification.ofErrorMessage(
                    FAILED_TO_BROWSE_LINK.formatted(validatedLink.link().originalUrl()));
        }
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
