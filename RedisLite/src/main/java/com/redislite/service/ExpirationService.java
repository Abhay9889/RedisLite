package com.redislite.service;

import com.redislite.core.RedisStore;
import com.redislite.core.StoreMetrics;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExpirationService {

    private final RedisStore store;
    private final StoreMetrics metrics;

    public ExpirationService(RedisStore store, StoreMetrics metrics) {
        this.store = store;
        this.metrics = metrics;
    }

    /** Runs in the background; expired entries are also removed when accessed. */
    @Scheduled(fixedDelayString = "${redislite.cleanup-interval-ms:30000}")
    public int cleanupExpiredEntries() {
        int removed = store.cleanupExpired();
        metrics.recordCleanup();
        return removed;
    }

    public int cleanupNow() {
        return cleanupExpiredEntries();
    }
}
