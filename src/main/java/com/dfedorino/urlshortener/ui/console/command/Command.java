package com.dfedorino.urlshortener.ui.console.command;

import com.dfedorino.urlshortener.domain.model.link.LinkDTO;
import com.dfedorino.urlshortener.domain.model.user.User;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import com.dfedorino.urlshortener.ui.console.Cli;
import com.dfedorino.urlshortener.ui.console.util.ConsoleUtils;
import com.dfedorino.urlshortener.ui.console.util.ValidationUtil;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

public sealed interface Command {

  Object apply(String... args);

  String key();

  String description();

  String example();

  @Slf4j
  @RequiredArgsConstructor
  final class CreateLink implements Command {

    public static final String KEY = "create";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public LinkDTO apply(String... commandAndArgs) {
      if (commandAndArgs.length != 3) {
        throw new IllegalArgumentException(
            "Command must contain exactly 2 arguments, example: '%s'".formatted(example()));
      }
      try {
        Long userId = userService.find(Cli.USER_UUID.get())
            .orElseGet(() -> userService.create(Cli.USER_UUID.get()))
            .getId();
        int redirectLimit = Integer.parseInt(commandAndArgs[2]);
        LinkDTO link = linkService.createLink(userId, commandAndArgs[1], redirectLimit);
        ConsoleUtils.printOutResult(Cli.USER_UUID.get(), link);
        return link;
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Redirect limit is not a valid integer: " + commandAndArgs[2]);
      }

    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Shorten the given URL, where URL is a valid URL";
    }

    @Override
    public String example() {
      return KEY + " https://skillfactory.ru/ 100";
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class Redirect implements Command {

    public static final String KEY = "redirect";
    public static final String INVALID_LINK_LOG_MESSAGE_PATTERN = "!!! {} !!!";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public LinkDTO apply(String... commandAndArgs) {
      if (commandAndArgs.length != 2) {
        throw new IllegalArgumentException(
            "Command must contain exactly 1 argument, example: '%s'".formatted(example()));
      }

      String link = commandAndArgs[1];
      if (!link.startsWith(Cli.SHORTENED_URL_PREFIX)) {
        log.error("!!! Invalid short link !!!");
        return null;
      }
      String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());

      User user = userService.find(Cli.USER_UUID.get()).orElseThrow();
      Optional<LinkValidationService.ValidatedLink> optionalValidatedLink = linkService.findValidatedLink(
          user.getId(), linkCode);

      if (optionalValidatedLink.isEmpty()) {
        log.info("!!! Link not found by code {}!!!", linkCode);
        return null;
      }

      LinkValidationService.ValidatedLink validatedLink = optionalValidatedLink.get();

      if (validatedLink.status() == LinkValidationService.Status.INVALID) {
        log.info(INVALID_LINK_LOG_MESSAGE_PATTERN, validatedLink.reasonWhyInvalid());
        return linkService.invalidateLink(validatedLink.link().userId(),
                validatedLink.link().code())
            .orElseThrow();
      }

      try {
        Desktop.getDesktop().browse(URI.create(validatedLink.link().originalUrl()));
        LinkDTO linkDTO = linkService.updateRedirectLimit(user.getId(), linkCode,
                validatedLink.link().redirectLimit() - 1)
            .orElseThrow();
        ConsoleUtils.printOutResult(Cli.USER_UUID.get(), linkDTO);
        return linkDTO;
      } catch (IOException e) {
        throw new IllegalArgumentException("Failed to browse", e);
      }
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Redirect with the given short URL to the original URL";
    }

    @Override
    public String example() {
      return KEY + " " + Cli.SHORTENED_URL_PREFIX + "AQ";
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class ListActiveLinks implements Command {

    public static final String KEY = "list";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public List<LinkDTO> apply(String... commandAndArgs) {
      if (commandAndArgs.length != 1) {
        throw new IllegalArgumentException("Command invalid, example: '%s'".formatted(example()));
      }

      User user = userService.find(Cli.USER_UUID.get()).orElseThrow();
      List<LinkDTO> validUserLinks = linkService.findValidUserLinks(user.getId());
      ConsoleUtils.printOutResult(Cli.USER_UUID.get(), validUserLinks.toArray(LinkDTO[]::new));
      return validUserLinks;
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "List active created links";
    }

    @Override
    public String example() {
      return KEY;
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class EditLinkUrl implements Command {

    public static final String KEY = "edit_url";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public Optional<LinkDTO> apply(String... commandAndArgs) {
      if (commandAndArgs.length != 3) {
        log.error("Command invalid, example: '{}'", example());
        return Optional.empty();
      }

      String link = commandAndArgs[1];

      if (!ValidationUtil.isValidShortLink(link)) {
        log.error("!!! Invalid short link !!!");
        return Optional.empty();
      }
      String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());
      String newUrl = commandAndArgs[2];

      if (!ValidationUtil.isValidUrl(newUrl)) {
        log.error("!!! Invalid URL !!!");
        return Optional.empty();
      }

      User user = userService.find(Cli.USER_UUID.get()).orElseThrow();
      Optional<LinkDTO> linkDTO = linkService.updateOriginalUrl(user.getId(), linkCode, newUrl);
      linkDTO.ifPresent(value -> ConsoleUtils.printOutResult(Cli.USER_UUID.get(), value));
      return linkDTO;
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Edit active link URL";
    }

    @Override
    public String example() {
      return KEY + Cli.SHORTENED_URL_PREFIX + "AQA -url https://skillfactory.ru";
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class EditLinkRedirectLimit implements Command {

    public static final String KEY = "edit_limit";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public Optional<LinkDTO> apply(String... commandAndArgs) {
      if (commandAndArgs.length != 3) {
        log.error("Command invalid, example: '{}'", example());
        return Optional.empty();
      }

      String link = commandAndArgs[1];

      if (!link.startsWith(Cli.SHORTENED_URL_PREFIX)) {
        log.error("!!! Invalid short link !!!");
        return Optional.empty();
      }
      String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());
      String newLimit = commandAndArgs[2];

      if (!isValidNumber(newLimit)) {
        log.error("!!! Invalid redirect limit, must be from 1 to {}", Integer.MAX_VALUE);
      }

      User user = userService.find(Cli.USER_UUID.get()).orElseThrow();
      Optional<LinkDTO> linkDTO = linkService.updateRedirectLimit(user.getId(), linkCode,
          Integer.parseInt(newLimit));
      linkDTO.ifPresent(value -> ConsoleUtils.printOutResult(Cli.USER_UUID.get(), value));
      return linkDTO;
    }

    private boolean isValidNumber(String newLimit) {
      try {
        return Integer.parseInt(newLimit) > 0;
      } catch (NumberFormatException ignored) {

      }
      return false;
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Edit active link redirect limit.";
    }

    @Override
    public String example() {
      return KEY + Cli.SHORTENED_URL_PREFIX + "AQA -url https://skillfactory.ru";
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class Delete implements Command {

    public static final String KEY = "delete";
    public static final String INVALID_LINK_LOG_MESSAGE_PATTERN = "!!! {} !!!";
    private final UserService userService;
    private final LinkService linkService;

    @Override
    public Optional<LinkDTO> apply(String... commandAndArgs) {
      if (commandAndArgs.length != 2) {
        log.error("Command invalid, example '{}'", example());
        return Optional.empty();
      }

      String link = commandAndArgs[1];
      if (!ValidationUtil.isValidShortLink(link)) {
        log.error("!!! Invalid short link !!!");
        return Optional.empty();
      }
      String linkCode = link.substring(Cli.SHORTENED_URL_PREFIX.length());

      User user = userService.find(Cli.USER_UUID.get()).orElseThrow();
      Optional<LinkDTO> linkDTO = linkService.deleteLink(user.getId(), linkCode);
      linkDTO.ifPresent(value -> ConsoleUtils.printOutResult(Cli.USER_UUID.get(), value));
      return linkDTO;
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Delete given link";
    }

    @Override
    public String example() {
      return KEY + " " + Cli.SHORTENED_URL_PREFIX + "AQ";
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  final class Login implements Command {

    public static final String KEY = "login";

    private final UserService userService;

    @Override
    public Optional<String> apply(String... commandAndArgs) {
      if (commandAndArgs.length != 2) {
        log.error("Command invalid, example: '{}'", example());
        return Optional.empty();
      }

      String uuid = commandAndArgs[1];

      try {
        UUID.fromString(uuid);
      } catch (IllegalArgumentException e) {
        log.error("!!! UUID invalid !!!");
        return Optional.empty();
      }

      if (userService.find(uuid).isEmpty()) {
        log.error("!!! User does not exist !!!");
        return Optional.empty();
      }

      Cli.USER_UUID.set(uuid);
      log.info(">> Login successful!");
      return Optional.of(uuid);
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Login with UUID";
    }

    @Override
    public String example() {
      return KEY + " UUID";
    }
  }

  final class Quit implements Command {

    public static final String KEY = "quit";

    @Override
    public Void apply(String... commandAndArgs) {
      System.exit(0);
      return null;
    }

    @Override
    public String key() {
      return KEY;
    }

    @Override
    public String description() {
      return "Exit";
    }

    @Override
    public String example() {
      return KEY;
    }
  }
}
