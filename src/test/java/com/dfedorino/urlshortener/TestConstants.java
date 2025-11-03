package com.dfedorino.urlshortener;

import com.dfedorino.urlshortener.ui.console.Cli;
import java.util.UUID;

public class TestConstants {


    public static final String SHORT_LINK = Cli.SHORTENED_URL_PREFIX + "AQ";
    public static final String VALID_URL = "https://ya.ru";
    public static final String INVALID_URL = "invalid_url";
    public static final String NEW_URL = "https://google.com";
    public static final String USER_UUID = UUID.randomUUID().toString();
    public static final long USER_ID = 1L;
    public static final int REDIRECT_LIMIT = 100;
    public static final int INVALID_REDIRECT_LIMIT = -1;
    public static final String NEW_REDIRECT_LIMIT = "10";
}
