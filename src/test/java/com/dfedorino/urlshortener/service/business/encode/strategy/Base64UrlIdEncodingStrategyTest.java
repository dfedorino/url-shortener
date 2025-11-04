package com.dfedorino.urlshortener.service.business.encode.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.urlshortener.service.business.encode.IdEncodingStrategy;
import org.junit.jupiter.api.Test;

class Base64UrlIdEncodingStrategyTest {
    private final IdEncodingStrategy encoder = new Base64UrlIdEncodingStrategy();

    @Test
    void encode() {
        String encoded = encoder.encode(12345L);
        assertThat(encoded).isEqualTo("MDk");
    }
}