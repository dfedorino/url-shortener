package com.dfedorino.urlshortener.ui.console.command.config;

import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.business.UserService;
import com.dfedorino.urlshortener.ui.console.command.Command;
import com.dfedorino.urlshortener.ui.console.command.impl.CreateLink;
import com.dfedorino.urlshortener.ui.console.command.impl.Delete;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkRedirectLimit;
import com.dfedorino.urlshortener.ui.console.command.impl.EditLinkUrl;
import com.dfedorino.urlshortener.ui.console.command.impl.ListActiveLinks;
import com.dfedorino.urlshortener.ui.console.command.impl.Login;
import com.dfedorino.urlshortener.ui.console.command.impl.Quit;
import com.dfedorino.urlshortener.ui.console.command.impl.Redirect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

    @Bean
    public Command createLink(UserService userService, LinkService linkService) {
        return new CreateLink(userService, linkService);
    }

    @Bean
    public Command redirect(UserService userService, LinkService linkService) {
        return new Redirect(userService, linkService);
    }

    @Bean
    public Command listLinks(UserService userService, LinkService linkService) {
        return new ListActiveLinks(userService, linkService);
    }

    @Bean
    public Command editLinkUrl(UserService userService, LinkService linkService) {
        return new EditLinkUrl(userService, linkService);
    }

    @Bean
    public Command editLinkRedirectLimit(UserService userService, LinkService linkService) {
        return new EditLinkRedirectLimit(userService, linkService);
    }

    @Bean
    public Command delete(UserService userService, LinkService linkService) {
        return new Delete(userService, linkService);
    }

    @Bean
    public Command login(UserService userService) {
        return new Login(userService);
    }

    @Bean
    public Command quit() {
        return new Quit();
    }
}
