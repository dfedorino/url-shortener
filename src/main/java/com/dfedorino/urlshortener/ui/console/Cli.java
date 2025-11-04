package com.dfedorino.urlshortener.ui.console;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.dto.ResultWithNotification;
import com.dfedorino.urlshortener.ui.console.command.impl.Quit;
import com.dfedorino.urlshortener.ui.console.util.ConsoleUtils;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Cli {

    public static final ThreadLocal<String> USER_UUID = ThreadLocal.withInitial(
            () -> UUID.randomUUID().toString());
    public static final String SHORTENED_URL_PREFIX = "https://u.rl/";
    public static final String DELIMETER = "=".repeat(50);
    public static final String TITLE = ">> Link Shortener Project <<";
    public static final String AVAILABLE_COMMANDS = "Available commands:";

    public static final String COMMAND_LINE_TEMPLATE = "* %s : %s, example: '%s'%n";
    public static final String INVALID_COMMAND = "!!! Invalid command !!!";

    private final List<Command<?>> commands;
    private final UserService userService;

    @SuppressWarnings("unchecked")
    public void start() {
        var keyToCommand = commands.stream()
                .collect(Collectors.toMap(Command::key, Function.identity()));

        log.info(TITLE);
        ConsoleUtils.printOutAvailableCommands(commands);
        userService.create(USER_UUID.get());

        var scanner = new Scanner(System.in);

        for (String line = scanner.nextLine(); !line.equalsIgnoreCase(Quit.KEY_TOKEN);
                line = scanner.nextLine()) {
            String[] tokens = line.split(" ");
            String command = tokens[0];
            if (!keyToCommand.containsKey(command)) {
                log.info(INVALID_COMMAND);
                ConsoleUtils.printOutAvailableCommands(commands);
            } else {
                ResultWithNotification<?> apply = keyToCommand.get(command).apply(tokens);
                log.info(apply.notification());
                apply.result().ifPresent(value -> {
                    if (value instanceof LinkDto link) {
                        ConsoleUtils.printOutResult(Cli.USER_UUID.get(), link);
                    } else if (value instanceof List list) {
                        List<LinkDto> links = (List<LinkDto>) list;
                        ConsoleUtils.printOutResult(Cli.USER_UUID.get(),
                                                    links.toArray(LinkDto[]::new));
                    } else {
                        log.info(">> user id: {}", Cli.USER_UUID.get());
                        log.info(value.toString());
                    }
                });
            }
        }
    }
}
