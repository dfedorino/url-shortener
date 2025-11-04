package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService.ValidatedLink;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteLink implements Command<LinkDto> {

    public static final String KEY_TOKEN = "delete";
    public static final String EXAMPLE_MESSAGE = KEY_TOKEN + " " + Cli.SHORTENED_URL_PREFIX + "AQ";
    public static final String SUCCESS_MESSAGE = "Link successfully deleted";
    public static final String DESCRIPTION_MESSAGE = "Delete given link";
    public static final String LINK_ALREADY_DELETED = "Link %s already deleted";
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

        String linkCode = commandAndArgs[1].substring(Cli.SHORTENED_URL_PREFIX.length());

        Optional<User> user = userService.find(Cli.USER_UUID.get());

        if (user.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.USER_NOT_FOUND_MESSAGE.formatted(Cli.USER_UUID.get()));
        }

        Optional<ValidatedLink> toBeDeleted = linkService.findValidatedLink(
                user.get().getId(), linkCode);

        if (toBeDeleted.isEmpty()) {
            return ResultWithNotification.ofErrorMessage(
                    Command.SHORT_LINK_NOT_FOUND_MESSAGE.formatted(commandAndArgs[1]));
        }

        ValidatedLink validatedLink = toBeDeleted.get();
        if (LinkStatus.DELETED.name().equals(validatedLink.link().status())) {
            return ResultWithNotification.ofErrorMessage(
                    LINK_ALREADY_DELETED.formatted(commandAndArgs[1]));
        }

        Optional<LinkDto> deleted = linkService.deleteLink(user.get().getId(), linkCode);
        return new ResultWithNotification<>(SUCCESS_MESSAGE, deleted);
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
