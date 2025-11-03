package com.dfedorino.urlshortener.service.housekeeping.cleanup.impl;

import com.dfedorino.urlshortener.domain.model.link.Link;
import com.dfedorino.urlshortener.domain.model.link.LinkStatus;
import com.dfedorino.urlshortener.domain.repository.housekeeping.HousekeepingLinkRepository;
import com.dfedorino.urlshortener.service.business.LinkService;
import com.dfedorino.urlshortener.service.housekeeping.cleanup.CleanupStrategy;
import com.dfedorino.urlshortener.service.validation.LinkValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IterativeCleanupStrategy implements CleanupStrategy {

    private final HousekeepingLinkRepository housekeepingLinkRepository;
    private final LinkService linkService;
    private final LinkValidationService linkValidationService;

    @Override
    public void cleanup() {
        try {
            log.info("Starting cleanup task...");

            for (Link link : housekeepingLinkRepository.findByStatusId(LinkStatus.ACTIVE.getId())) {
                LinkValidationService.ValidatedLink validatedLink = linkValidationService.validate(
                        link);
                if (validatedLink.status() == LinkValidationService.Status.INVALID) {
                    linkService.invalidateLink(link.getUserId(), link.getCode());
                    log.debug("Marked link as INVALID, code: {}", link.getCode());
                }
            }
            log.info("Cleanup finished.");
        } catch (Exception e) {
            log.error("Error during cleanup", e);
        }
    }
}
