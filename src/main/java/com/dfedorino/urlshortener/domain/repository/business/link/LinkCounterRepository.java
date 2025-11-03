package com.dfedorino.urlshortener.domain.repository.business.link;

public interface LinkCounterRepository {

    Long incrementAndGet();
}
