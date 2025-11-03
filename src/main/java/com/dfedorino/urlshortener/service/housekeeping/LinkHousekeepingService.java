package com.dfedorino.urlshortener.service.housekeeping;

import com.dfedorino.urlshortener.service.housekeeping.cleanup.CleanupStrategy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO: write tests!!!
@Slf4j
@RequiredArgsConstructor
public class LinkHousekeepingService {

    private final CleanupStrategy cleanupStrategy;
    private final ScheduledExecutorService scheduler;
    private final long intervalMs;

    public void start() {
        log.info(">> Housekeeping started");
        scheduler.scheduleAtFixedRate(cleanupStrategy::cleanup, intervalMs, intervalMs,
                                      TimeUnit.MILLISECONDS);
    }

    public void stop() {
        log.info(">> Housekeeping stopped");
        scheduler.shutdown();
    }
}

