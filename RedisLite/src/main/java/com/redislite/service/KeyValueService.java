package com.redislite.service;

import com.redislite.core.RedisStore;
import com.redislite.core.StoreMetrics;
import com.redislite.model.CacheEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class KeyValueService {

    private final RedisStore store;
    private final StoreMetrics metrics;

    public KeyValueService(RedisStore store, StoreMetrics metrics) {
        this.store = store;
        this.metrics = metrics;
    }

    public boolean set(String key, String value) {
        boolean created = store.set(key, value);
        metrics.recordSet();
        return created;
    }

    public boolean set(String key, String value, long ttlSeconds) {
        validateTtlSeconds(ttlSeconds);
        boolean created = store.set(key, value, Duration.ofSeconds(ttlSeconds));
        metrics.recordSet();
        return created;
    }

    public Optional<CacheEntry> get(String key) {
        metrics.recordGet();
        return store.getEntry(key);
    }

    public boolean exists(String key) {
        metrics.recordExists();
        return store.exists(key);
    }

    public boolean delete(String key) {
        metrics.recordDelete();
        return store.delete(key);
    }

    public boolean expire(String key, long ttlSeconds) {
        validateTtlSeconds(ttlSeconds);
        metrics.recordExpire();
        return store.expire(key, Duration.ofSeconds(ttlSeconds));
    }

    public boolean persist(String key) {
        metrics.recordPersist();
        return store.persist(key);
    }

    private void validateTtlSeconds(long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be greater than zero.");
        }
    }
}
