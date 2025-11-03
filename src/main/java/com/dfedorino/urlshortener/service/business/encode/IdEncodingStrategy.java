package com.dfedorino.urlshortener.service.business.encode;

public interface IdEncodingStrategy {

    String encode(long id);
}
