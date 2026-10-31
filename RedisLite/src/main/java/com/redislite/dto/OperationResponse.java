package com.redislite.dto;

public record OperationResponse(boolean success, String message, String key, Boolean created) { }
