package com.redislite.dto;

import java.time.Instant;

public record ValueResponse(String key, String value, Instant createdAt, boolean expiring) { }
