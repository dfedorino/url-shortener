package com.dfedorino.urlshortener.service.business.encode.strategy;

import com.dfedorino.urlshortener.service.business.encode.IdEncodingStrategy;
import java.nio.ByteBuffer;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * A Base64 URL-safe implementation of the {@link IdEncodingStrategy}.
 * <p>
 * This strategy encodes a numeric identifier ({@code long}) into a compact Base64 string that can
 * safely be used in URLs (using {@link Base64#getUrlEncoder()}). Padding characters are removed to
 * reduce length.
 * </p>
 * <p>
 * The encoding process uses the minimal number of bytes required to represent the {@code long}
 * value, ensuring that shorter IDs produce shorter encoded strings.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * IdEncodingStrategy encoder = new Base64UrlIdEncodingStrategy();
 * String encoded = encoder.encode(12345L); // e.g. "MDk"
 * }</pre>
 */
@Component
public class Base64UrlIdEncodingStrategy implements IdEncodingStrategy {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    /**
     * Encodes the provided {@code long} identifier into a URL-safe Base64 string.
     * <p>
     * The number of bytes used to represent the ID is minimized based on the number of significant
     * bits in the {@code long} value. For example, smaller IDs will use fewer bytes and thus
     * produce shorter encoded strings.
     * </p>
     *
     * @param id the numeric identifier to encode; must be greater than zero
     * @return a compact, URL-safe Base64 string representing the given ID
     */
    @Override
    public String encode(long id) {
        int bytesToStoreId = (int) Math.ceil((Long.SIZE - Long.numberOfLeadingZeros(id)) / 8.0);
        byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(id).array();
        byte[] minimalBytesRequired = new byte[bytesToStoreId];
        System.arraycopy(bytes, Long.BYTES - bytesToStoreId, minimalBytesRequired, 0,
                         bytesToStoreId);
        return ENCODER.encodeToString(minimalBytesRequired);
    }
}
