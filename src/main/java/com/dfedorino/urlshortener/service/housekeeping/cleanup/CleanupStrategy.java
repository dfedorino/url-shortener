package com.dfedorino.urlshortener.service.housekeeping.cleanup;

/**
 * Defines a strategy for performing cleanup operations.
 * <p>
 * Implementations of this interface encapsulate specific logic
 * for invalid entities in the system.
 */
public interface CleanupStrategy {

    /**
     * Executes the cleanup process according to the strategy's implementation.
     * <p>
     * This method should contain all necessary steps to identify and remove
     * or update invalid data.
     */
    void cleanup();
}
