package com.dfedorino.urlshortener.domain.repository.housekeeping;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import java.util.List;

/**
 * Repository interface dedicated to housekeeping operations on {@link Link} entities.
 * <p>
 * Provides methods for querying and updating link statuses, typically used by maintenance services
 * (e.g., link expiration or cleanup tasks).
 * </p>
 */
public interface HousekeepingLinkRepository {

    /**
     * Retrieves all links that have the specified {@link LinkStatus} ID.
     *
     * @param statusId the identifier of the link status
     * @return a list of {@link Link} entities matching the provided status ID; may be empty if no
     * links are found
     */
    List<Link> findByStatusId(Long statusId);

    /**
     * Updates the status of a link by its unique identifier.
     *
     * @param linkId      the unique identifier of the link to update
     * @param newStatusId the new {@link LinkStatus} ID to assign to the link
     */
    void updateStatusId(Long linkId, Long newStatusId);
}
