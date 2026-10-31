package com.redislite.dto;

public record StatsResponse(String store, int currentEntries, int maxEntries,
                            double capacityUsedPercent, long setOperations,
                            long getOperations, long deleteOperations,
                            long existsChecks, long expireCalls,
                            long persistCalls, long cleanupRuns) { }
