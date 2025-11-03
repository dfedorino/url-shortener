package com.dfedorino.urlshortener.service.business.encode.strategy;

import com.dfedorino.urlshortener.service.business.encode.IdEncodingStrategy;
import java.nio.ByteBuffer;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class Base64UrlIdEncodingStrategy implements IdEncodingStrategy {

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  @Override
  public String encode(long id) {
    int bytesToStoreId = (int) Math.ceil((Long.SIZE - Long.numberOfLeadingZeros(id)) / 8.0);
    byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(id).array();
    byte[] minimalBytesRequired = new byte[bytesToStoreId];
    System.arraycopy(bytes, Long.BYTES - bytesToStoreId, minimalBytesRequired, 0, bytesToStoreId);
    return ENCODER.encodeToString(minimalBytesRequired);
  }
}
