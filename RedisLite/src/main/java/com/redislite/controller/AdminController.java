package com.redislite.controller;

import com.redislite.core.RedisStore;
import com.redislite.service.ExpirationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final RedisStore store;
    private final ExpirationService expirationService;

    public AdminController(RedisStore store, ExpirationService expirationService) {
        this.store = store;
        this.expirationService = expirationService;
    }

    @PostMapping("/cleanup")
    public Map<String, Object> cleanup() {
        int removed = expirationService.cleanupNow();
        return Map.of("success", true, "removedExpiredEntries", removed);
    }

    @DeleteMapping("/keys")
    public Map<String, Object> clearAll() {
        int removed = store.clear();
        return Map.of("success", true, "message", "All keys cleared.", "removedEntries", removed);
    }
}
