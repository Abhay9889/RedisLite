package com.redislite.dto;

public record TtlResponse(String key, boolean exists, boolean expiring,
                          Long remainingTtlMillis, String message) { }
