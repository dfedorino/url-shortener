package com.dfedorino.urlshortener.domain.repository.business.link;

/**
 * Repository interface responsible for managing a persistent counter
 * used to generate unique, sequential link identifiers.
 */
public interface LinkCounterRepository {

    /**
     * Atomically increments the stored counter and returns the new value.
     * <p>
     * Implementations of this method must ensure thread safety and
     * consistency across concurrent invocations, typically by relying on
     * database transactions or atomic operations.
     * </p>
     *
     * @return the incremented counter value after the operation
     */
    Long incrementAndGet();
}
