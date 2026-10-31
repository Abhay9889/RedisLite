package com.redislite.config;

import com.redislite.core.RedisStore;
import com.redislite.core.StoreConfig;
import com.redislite.core.StoreMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisStoreConfiguration {

    @Bean
    public StoreConfig storeConfig(
            @Value("${redislite.store.max-entries:100000}") int maxEntries,
            @Value("${redislite.store.max-key-length:256}") int maxKeyLength,
            @Value("${redislite.store.max-value-length:1048576}") int maxValueLength,
            @Value("${redislite.store.max-ttl-seconds:31536000}") long maxTtlSeconds) {
        return new StoreConfig(maxEntries, maxKeyLength, maxValueLength, maxTtlSeconds);
    }

    @Bean
    public StoreMetrics storeMetrics() {
        return new StoreMetrics();
    }

    @Bean
    public RedisStore redisStore(StoreConfig config) {
        return new RedisStore(config);
    }
}
