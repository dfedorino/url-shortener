package com.dfedorino.urlshortener.ui.console.command.config;

import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.command.Command;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

  @Bean
  public Command createLink(UserService userService, LinkService linkService) {
    return new Command.CreateLink(userService, linkService);
  }

  @Bean
  public Command redirect(UserService userService, LinkService linkService) {
    return new Command.Redirect(userService, linkService);
  }

  @Bean
  public Command listLinks(UserService userService, LinkService linkService) {
    return new Command.ListActiveLinks(userService, linkService);
  }

  @Bean
  public Command editLinkUrl(UserService userService, LinkService linkService) {
    return new Command.EditLinkUrl(userService, linkService);
  }

  @Bean
  public Command editLinkRedirectLimit(UserService userService, LinkService linkService) {
    return new Command.EditLinkRedirectLimit(userService, linkService);
  }

  @Bean
  public Command delete(UserService userService, LinkService linkService) {
    return new Command.Delete(userService, linkService);
  }

  @Bean
  public Command login(UserService userService) {
    return new Command.Login(userService);
  }

  @Bean
  public Command quit() {
    return new Command.Quit();
  }
}
