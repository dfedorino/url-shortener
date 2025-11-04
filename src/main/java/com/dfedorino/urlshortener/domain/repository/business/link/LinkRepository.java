package com.dfedorino.urlshortener.domain.repository.business.link;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Link} entities.
 * <p>
 * Provides persistence operations for creating, retrieving, and updating links associated with
 * specific users.
 * </p>
 */
public interface LinkRepository {

    /**
     * Persists a new {@link Link} entity.
     *
     * @param link the link entity to save
     * @return the saved {@link Link} instance with an assigned ID
     */
    Link save(Link link);

    /**
     * Finds a link by its user ID and unique code.
     *
     * @param userId the ID of the user who owns the link
     * @param code   the unique short code of the link
     * @return an {@link Optional} containing the link if found
     */
    Optional<Link> findByUserIdAndCode(Long userId, String code);

    /**
     * Retrieves all links associated with a given user.
     *
     * @param userId the ID of the user
     * @return a list of {@link Link} entities belonging to the user
     */
    List<Link> findByUserId(Long userId);

    /**
     * Retrieves all links associated with a given user having given status.
     *
     * @param userId   the ID of the user
     * @param statusId the {@link LinkStatus} ID
     * @return a list of {@link Link} entities matching the criteria
     */
    List<Link> findByUserIdAndStatusId(Long userId, Long statusId);

    /**
     * Updates the original URL of a link identified by user ID and code.
     *
     * @param userId         the ID of the user
     * @param code           the link's unique code
     * @param newOriginalUrl the new URL to associate with the link
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean updateOriginalUrlByUserIdAndCode(Long userId, String code, String newOriginalUrl);

    /**
     * Updates the redirect limit of a link identified by user ID and code.
     *
     * @param userId           the ID of the user
     * @param code             the link's unique code
     * @param newRedirectLimit the new redirect limit
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean updateRedirectLimitByUserIdAndCode(Long userId, String code, Integer newRedirectLimit);

    /**
     * Updates the status of a link identified by user ID and code.
     *
     * @param userId      the ID of the user
     * @param code        the link's unique code
     * @param newStatusId the new {@link LinkStatus} ID
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    boolean updateStatusIdByUserIdAndCode(Long userId, String code, Long newStatusId);

    /**
     * Retrieves all links with the given status ID.
     *
     * @param id the {@link LinkStatus} ID
     * @return a list of {@link Link} entities with the given status
     */
    List<Link> findByStatusId(Long id);

    /**
     * Finds a link by its user ID and original URL.
     *
     * @param userId      the ID of the user
     * @param originalUrl the original URL of the link
     * @return an {@link Optional} containing the link if found
     */
    Optional<Link> findByUserIdAndOriginalUrl(Long userId, String originalUrl);
}
