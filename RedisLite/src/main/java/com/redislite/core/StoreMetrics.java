package com.redislite.core;

import java.util.concurrent.atomic.LongAdder;

/** Lightweight operation counters for the demo API. */
public final class StoreMetrics {

    private final LongAdder sets = new LongAdder();
    private final LongAdder gets = new LongAdder();
    private final LongAdder deletes = new LongAdder();
    private final LongAdder existsChecks = new LongAdder();
    private final LongAdder expireCalls = new LongAdder();
    private final LongAdder persistCalls = new LongAdder();
    private final LongAdder cleanupRuns = new LongAdder();

    public void recordSet() { sets.increment(); }
    public void recordGet() { gets.increment(); }
    public void recordDelete() { deletes.increment(); }
    public void recordExists() { existsChecks.increment(); }
    public void recordExpire() { expireCalls.increment(); }
    public void recordPersist() { persistCalls.increment(); }
    public void recordCleanup() { cleanupRuns.increment(); }

    public long getSets() { return sets.sum(); }
    public long getGets() { return gets.sum(); }
    public long getDeletes() { return deletes.sum(); }
    public long getExistsChecks() { return existsChecks.sum(); }
    public long getExpireCalls() { return expireCalls.sum(); }
    public long getPersistCalls() { return persistCalls.sum(); }
    public long getCleanupRuns() { return cleanupRuns.sum(); }
}
