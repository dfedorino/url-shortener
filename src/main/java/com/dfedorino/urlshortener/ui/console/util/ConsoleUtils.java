package com.dfedorino.urlshortener.ui.console.util;

import com.dfedorino.urlshortener.domain.model.link.LinkDto;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.command.Command;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ConsoleUtils {

    public void printOutResult(String uuid, LinkDto... result) {
        log.info(">> user id: {}", uuid);

        for (LinkDto linkDTO : result) {
            log.info(Cli.DELIMETER);
            log.info(">> status: {}", linkDTO.status());
            log.info(">> shortened url: {}", Cli.SHORTENED_URL_PREFIX + linkDTO.code());
            log.info(">> full url: {}", linkDTO.originalUrl());
            log.info(">>  redirect limit: {}", linkDTO.redirectLimit());
        }


    }

    public void printOutAvailableCommands(List<Command<?>> commands) {
        log.info(Cli.AVAILABLE_COMMANDS);

        commands.forEach(command -> System.out.printf(Cli.COMMAND_LINE_TEMPLATE,
                                                      command.key(),
                                                      command.description(),
                                                      command.example())
        );
    }
}
