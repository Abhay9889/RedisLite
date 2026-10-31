package com.redislite.service;

import com.redislite.core.RedisStore;
import com.redislite.core.StoreConfig;
import com.redislite.core.StoreMetrics;
import com.redislite.dto.StatsResponse;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final RedisStore store;
    private final StoreConfig config;
    private final StoreMetrics metrics;

    public MetricsService(RedisStore store, StoreConfig config, StoreMetrics metrics) {
        this.store = store;
        this.config = config;
        this.metrics = metrics;
    }

    public StatsResponse getStats() {
        int current = store.size();
        double percent = Math.round((current * 10000.0 / config.getMaxEntries())) / 100.0;
        return new StatsResponse(
                "RedisLite", current, config.getMaxEntries(), percent,
                metrics.getSets(), metrics.getGets(), metrics.getDeletes(),
                metrics.getExistsChecks(), metrics.getExpireCalls(),
                metrics.getPersistCalls(), metrics.getCleanupRuns()
        );
    }
}
