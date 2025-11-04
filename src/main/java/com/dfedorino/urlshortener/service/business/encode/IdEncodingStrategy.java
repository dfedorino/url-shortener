package com.dfedorino.urlshortener.service.business.encode;

/**
 * Defines a strategy for encoding numeric identifiers into short strings.
 * <p>
 * Implementations must use encoding algorithms to transform a long-based identifier into a short,
 * URL-safe string representation to be used as an identifier of the short link.
 * </p>
 */
public interface IdEncodingStrategy {

    /**
     * Encodes the given numeric identifier into a string representation.
     *
     * @param id the numeric identifier to encode; must be a positive value
     * @return the encoded string representation of the provided identifier
     */
    String encode(long id);
}
