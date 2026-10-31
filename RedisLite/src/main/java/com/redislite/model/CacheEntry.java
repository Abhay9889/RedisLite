package com.redislite.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** One stored value and its optional time-to-live metadata. */
public final class CacheEntry {

    private final String value;
    private final Instant createdAt;
    private final boolean expiring;
    private final long expiresAtNanos;

    private CacheEntry(String value, Instant createdAt, Duration ttl) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        this.value = value;
        this.createdAt = createdAt;
        this.expiring = ttl != null;

        if (ttl == null) {
            this.expiresAtNanos = 0L;
        } else {
            if (ttl.isZero() || ttl.isNegative()) {
                throw new IllegalArgumentException("TTL must be greater than zero.");
            }
            final long ttlNanos;
            try {
                ttlNanos = ttl.toNanos();
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("TTL is too large.", exception);
            }
            this.expiresAtNanos = System.nanoTime() + ttlNanos;
        }
    }

    public static CacheEntry permanent(String value) {
        return new CacheEntry(value, Instant.now(), null);
    }

    public static CacheEntry expiring(String value, Duration ttl) {
        if (ttl == null) {
            throw new IllegalArgumentException("TTL cannot be null.");
        }
        return new CacheEntry(value, Instant.now(), ttl);
    }

    public CacheEntry withExpiration(Duration ttl) {
        return new CacheEntry(value, createdAt, ttl);
    }

    public CacheEntry withoutExpiration() {
        return new CacheEntry(value, createdAt, null);
    }

    public String getValue() { return value; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean hasExpiration() { return expiring; }

    public boolean isExpired() {
        return expiring && System.nanoTime() - expiresAtNanos >= 0;
    }

    public Optional<Duration> getRemainingTtl() {
        if (!expiring) {
            return Optional.empty();
        }
        long remainingNanos = expiresAtNanos - System.nanoTime();
        return Optional.of(Duration.ofNanos(Math.max(0L, remainingNanos)));
    }
}
