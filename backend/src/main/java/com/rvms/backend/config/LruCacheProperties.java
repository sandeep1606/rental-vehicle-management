package com.rvms.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache.lru")
public record LruCacheProperties(
        boolean enabled,
        int maxSize,
        long ttlSeconds,
        boolean useRedis
) {}
