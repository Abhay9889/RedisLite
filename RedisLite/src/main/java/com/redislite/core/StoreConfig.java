package com.redislite.core;

/** Immutable limits used by the in-memory store. */
public final class StoreConfig {

    private final int maxEntries;
    private final int maxKeyLength;
    private final int maxValueLength;
    private final long maxTtlSeconds;

    public StoreConfig(int maxEntries, int maxKeyLength,
                       int maxValueLength, long maxTtlSeconds) {
        if (maxEntries <= 0 || maxKeyLength <= 0 || maxValueLength <= 0
                || maxTtlSeconds <= 0) {
            throw new IllegalArgumentException("All store limits must be greater than zero.");
        }
        this.maxEntries = maxEntries;
        this.maxKeyLength = maxKeyLength;
        this.maxValueLength = maxValueLength;
        this.maxTtlSeconds = maxTtlSeconds;
    }

    public int getMaxEntries() { return maxEntries; }
    public int getMaxKeyLength() { return maxKeyLength; }
    public int getMaxValueLength() { return maxValueLength; }
    public long getMaxTtlSeconds() { return maxTtlSeconds; }
}
