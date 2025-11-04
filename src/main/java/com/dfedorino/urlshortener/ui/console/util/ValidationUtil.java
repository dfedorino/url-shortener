package com.dfedorino.urlshortener.ui.console.util;

import com.dfedorino.urlshortener.ui.console.Cli;
import java.net.URI;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {

    public boolean isValidShortLink(String shortLink) {
        return shortLink.startsWith(Cli.SHORTENED_URL_PREFIX);
    }

    public boolean isValidUrl(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidLimit(String limit) {
        try {
            return Integer.parseInt(limit) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
