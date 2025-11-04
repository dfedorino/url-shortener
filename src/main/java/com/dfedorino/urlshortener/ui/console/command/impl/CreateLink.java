package com.dfedorino.urlshortener.ui.console.command.impl;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService.Status;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateLink implements Command<LinkDto> {

    public static final String KEY_TOKEN = "create";
    public static final String EXAMPLE_MESSAGE = KEY_TOKEN + " https://skillfactory.ru/ 100";
    public static final String SUCCESS_MESSAGE = "Link successfully created!";
    public static final String DUPLICATE_MESSAGE = "Link with URL %s already exists!";
    public static final String DESCRIPTION_MESSAGE = "Shorten the given URL";
    private final UserService userService;
    private final LinkService linkService;
    private final Environment environment;

    @Override
    public ResultWithNotification<LinkDto> apply(String... commandAndArgs) {
        var failedCheck = validateInOrder(List.of(
                () -> validateArguments(args -> args.length == 2 || args.length == 3,
                                        commandAndArgs),
                () -> validateOriginalUrl(commandAndArgs[1])
        ));

        if (failedCheck.isEmpty() && commandAndArgs.length == 3) {
            failedCheck = validateLimit(commandAndArgs[2]);
        }

        if (failedCheck.isPresent()) {
            return failedCheck.get();
        }

        Long userId = userService.find(Cli.USER_UUID.get())
                .orElseGet(() -> userService.create(Cli.USER_UUID.get())).getId();

        String originalUrl = commandAndArgs[1];
        int redirectLimit = commandAndArgs.length == 2 ?
                environment.getRequiredProperty("default-redirect-limit", Integer.class) :
                Integer.parseInt(commandAndArgs[2]);

        var possibleDuplicate = linkService.findValidatedLinkByOriginalUrl(userId, originalUrl);

        if (possibleDuplicate.isPresent() && possibleDuplicate.get().status() == Status.VALID) {
            return ResultWithNotification.ofErrorMessage(
                    CreateLink.DUPLICATE_MESSAGE.formatted(originalUrl));
        }

        LinkDto link = linkService.createLink(userId,
                                              originalUrl,
                                              redirectLimit);

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
