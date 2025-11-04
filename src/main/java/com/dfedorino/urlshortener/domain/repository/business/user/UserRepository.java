package com.dfedorino.urlshortener.domain.repository.business.user;

import com.dfedorino.urlshortener.domain.model.user.User;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Provides persistence operations for storing and retrieving users.
 * </p>
 */
public interface UserRepository {

    /**
     * Persists a new {@link User} entity.
     *
     * @param user the user entity to save
     * @return the saved {@link User} instance with an assigned ID
     */
    User save(User user);

    /**
     * Finds a user by their unique UUID.
     *
     * @param uuid the unique identifier of the user
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByUuid(String uuid);
}
