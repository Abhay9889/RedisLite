package com.redislite.core;

import com.redislite.model.CacheEntry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The storage engine. Reads use ConcurrentHashMap directly; write-side capacity
 * changes are coordinated with a small lock so the configured limit is respected.
 */
public class RedisStore {

    private final ConcurrentHashMap<String, CacheEntry> storage = new ConcurrentHashMap<>();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final StoreConfig config;

    public RedisStore(StoreConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("StoreConfig cannot be null.");
        }
        this.config = config;
    }

    /** Stores a value. Returns true when a new live key was created. */
    public boolean set(String key, String value) {
        validateKey(key);
        validateValue(value);
        return putEntry(key, CacheEntry.permanent(value));
    }

    /** Stores a value with a time-to-live. */
    public boolean set(String key, String value, Duration ttl) {
        validateKey(key);
        validateValue(value);
        validateTtl(ttl);
        return putEntry(key, CacheEntry.expiring(value, ttl));
    }

    private boolean putEntry(String key, CacheEntry newEntry) {
        writeLock.lock();
        try {
            CacheEntry current = storage.get(key);
            if (current != null && current.isExpired()) {
                storage.remove(key, current);
                current = null;
            }

            if (current == null && storage.size() >= config.getMaxEntries()) {
                cleanupExpiredLocked();
            }
            if (current == null && storage.size() >= config.getMaxEntries()) {
                throw new IllegalStateException("Storage capacity reached.");
            }

            storage.put(key, newEntry);
            return current == null;
        } finally {
            writeLock.unlock();
        }
    }

    public Optional<CacheEntry> getEntry(String key) {
        validateKey(key);
        CacheEntry entry = storage.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            storage.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    public boolean delete(String key) {
        validateKey(key);
        writeLock.lock();
        try {
            CacheEntry removed = storage.remove(key);
            return removed != null && !removed.isExpired();
        } finally {
            writeLock.unlock();
        }
    }

    public boolean exists(String key) {
        return getEntry(key).isPresent();
    }

    public boolean expire(String key, Duration ttl) {
        validateKey(key);
        validateTtl(ttl);
        writeLock.lock();
        try {
            CacheEntry current = storage.get(key);
            if (current == null) return false;
            if (current.isExpired()) {
                storage.remove(key, current);
                return false;
            }
            storage.put(key, current.withExpiration(ttl));
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean persist(String key) {
        validateKey(key);
        writeLock.lock();
        try {
            CacheEntry current = storage.get(key);
            if (current == null) return false;
            if (current.isExpired()) {
                storage.remove(key, current);
                return false;
            }
            storage.put(key, current.withoutExpiration());
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public int size() {
        cleanupExpired();
        return storage.size();
    }

    public int clear() {
        writeLock.lock();
        try {
            int removed = storage.size();
            storage.clear();
            return removed;
        } finally {
            writeLock.unlock();
        }
    }

    public int cleanupExpired() {
        writeLock.lock();
        try {
            return cleanupExpiredLocked();
        } finally {
            writeLock.unlock();
        }
    }

    private int cleanupExpiredLocked() {
        int removed = 0;
        for (Map.Entry<String, CacheEntry> item : storage.entrySet()) {
            CacheEntry entry = item.getValue();
            if (entry.isExpired() && storage.remove(item.getKey(), entry)) {
                removed++;
            }
        }
        return removed;
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be blank.");
        }
        if (key.length() > config.getMaxKeyLength()) {
            throw new IllegalArgumentException("Key exceeds the configured length limit.");
        }
    }

    private void validateValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        if (value.length() > config.getMaxValueLength()) {
            throw new IllegalArgumentException("Value exceeds the configured length limit.");
        }
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must be greater than zero.");
        }
        if (ttl.compareTo(Duration.ofSeconds(config.getMaxTtlSeconds())) > 0) {
            throw new IllegalArgumentException("TTL exceeds the configured maximum.");
        }
        try {
            ttl.toNanos();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("TTL is too large.", exception);
        }
    }
}
