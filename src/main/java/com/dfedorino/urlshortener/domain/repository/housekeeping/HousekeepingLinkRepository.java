package com.dfedorino.urlshortener.domain.repository.housekeeping;

import com.dfedorino.urlshortener.domain.model.link.Link;
import java.util.List;

public interface HousekeepingLinkRepository {

    List<Link> findByStatusId(Long statusId);

    void updateStatusId(Long linkId, Long newStatusId);
}
