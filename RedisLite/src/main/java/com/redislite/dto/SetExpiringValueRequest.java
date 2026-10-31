package com.redislite.dto;

public record SetExpiringValueRequest(String key, String value, Long ttlSeconds) { }
