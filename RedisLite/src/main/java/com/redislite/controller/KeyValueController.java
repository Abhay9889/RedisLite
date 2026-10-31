package com.redislite.controller;

import com.redislite.dto.*;
import com.redislite.model.CacheEntry;
import com.redislite.service.KeyValueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/keys")
public class KeyValueController {

    private final KeyValueService service;

    public KeyValueController(KeyValueService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OperationResponse> set(@RequestBody SetValueRequest request) {
        requireRequest(request);
        boolean created = service.set(request.key(), request.value());
        OperationResponse response = new OperationResponse(
                true, created ? "Key stored." : "Key updated.", request.key(), created);
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @PostMapping("/expiring")
    public ResponseEntity<OperationResponse> setExpiring(
            @RequestBody SetExpiringValueRequest request) {
        requireRequest(request);
        if (request.ttlSeconds() == null) {
            throw new IllegalArgumentException("ttlSeconds is required.");
        }
        boolean created = service.set(request.key(), request.value(), request.ttlSeconds());
        OperationResponse response = new OperationResponse(
                true, created ? "Expiring key stored." : "Key updated with expiration.",
                request.key(), created);
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @GetMapping("/{key}")
    public ValueResponse get(@PathVariable String key) {
        CacheEntry entry = findEntry(key);
        return toValueResponse(key, entry);
    }

    @GetMapping("/{key}/exists")
    public ExistsResponse exists(@PathVariable String key) {
        return new ExistsResponse(key, service.exists(key));
    }

    @DeleteMapping("/{key}")
    public OperationResponse delete(@PathVariable String key) {
        boolean deleted = service.delete(key);
        if (!deleted) {
            throw new java.util.NoSuchElementException("Key was not found: " + key);
        }
        return new OperationResponse(true, "Key deleted.", key, null);
    }

    @PostMapping("/{key}/expire")
    public OperationResponse expire(@PathVariable String key, @RequestBody TtlRequest request) {
        requireRequest(request);
        if (request.ttlSeconds() == null) {
            throw new IllegalArgumentException("ttlSeconds is required.");
        }
        boolean updated = service.expire(key, request.ttlSeconds());
        if (!updated) {
            throw new java.util.NoSuchElementException("Key was not found: " + key);
        }
        return new OperationResponse(true, "Expiration applied.", key, null);
    }

    @PostMapping("/{key}/persist")
    public OperationResponse persist(@PathVariable String key) {
        boolean updated = service.persist(key);
        if (!updated) {
            throw new java.util.NoSuchElementException("Key was not found: " + key);
        }
        return new OperationResponse(true, "Expiration removed; key is now permanent.", key, null);
    }

    @GetMapping("/{key}/ttl")
    public TtlResponse ttl(@PathVariable String key) {
        CacheEntry entry = findEntry(key);
        Optional<Duration> remaining = entry.getRemainingTtl();
        if (remaining.isEmpty()) {
            return new TtlResponse(key, true, false, null, "Key does not expire.");
        }
        long millis = Math.max(0L, remaining.get().toMillis());
        return new TtlResponse(key, true, true, millis, "Remaining time to live.");
    }

    private CacheEntry findEntry(String key) {
        return service.get(key).orElseThrow(
                () -> new java.util.NoSuchElementException("Key was not found: " + key));
    }

    private ValueResponse toValueResponse(String key, CacheEntry entry) {
        return new ValueResponse(key, entry.getValue(), entry.getCreatedAt(), entry.hasExpiration());
    }

    private void requireRequest(Object request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
    }
}
