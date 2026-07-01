package com.rvms.backend.cache;

import com.rvms.backend.config.LruCacheProperties;
import com.rvms.backend.dto.customer.CustomerResponse;
import com.rvms.backend.mapper.CustomerMapper;
import com.rvms.backend.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * LRU-cached customer search, backed by Redis (sorted-set recency index + string entries)
 * when available, and an in-memory {@link InMemoryLruCache} fallback otherwise.
 * Search terms match against name, email, phone, or driver license number.
 */
@Service
@Slf4j
public class CustomerSearchCacheService {

    private static final String INDEX_KEY = "customer:search:lru:index";
    private static final String ENTRY_PREFIX = "customer:search:entry:";

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final LruCacheProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InMemoryLruCache<String, List<CustomerResponse>> localCache;

    public CustomerSearchCacheService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            LruCacheProperties properties,
            ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.properties = properties;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.localCache = new InMemoryLruCache<>(properties.maxSize() > 0 ? properties.maxSize() : 50);
    }

    public List<CustomerResponse> search(String rawTerm) {
        String term = normalize(rawTerm);
        if (term.isEmpty()) {
            return List.of();
        }
        if (!properties.enabled()) {
            return queryAndMap(term);
        }

        List<CustomerResponse> cached = getFromCache(term);
        if (cached != null) {
            log.debug("LRU cache HIT for customer search term '{}'", term);
            return cached;
        }

        log.debug("LRU cache MISS for customer search term '{}'", term);
        List<CustomerResponse> result = queryAndMap(term);
        putInCache(term, result);
        return result;
    }

    private List<CustomerResponse> queryAndMap(String term) {
        return customerRepository.searchByTerm(term).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    private boolean useRedis() {
        return properties.useRedis() && redisTemplate != null;
    }

    @SuppressWarnings("unchecked")
    private List<CustomerResponse> getFromCache(String term) {
        if (useRedis()) {
            try {
                Object value = redisTemplate.opsForValue().get(ENTRY_PREFIX + term);
                if (value != null) {
                    touchRedis(term);
                    return (List<CustomerResponse>) value;
                }
                return null;
            } catch (Exception e) {
                log.warn("Redis unavailable ({}), falling back to in-memory LRU cache", e.getMessage());
                return localCache.get(term);
            }
        }
        return localCache.get(term);
    }

    private void putInCache(String term, List<CustomerResponse> result) {
        if (useRedis()) {
            try {
                redisTemplate.opsForValue().set(ENTRY_PREFIX + term, result, Duration.ofSeconds(properties.ttlSeconds()));
                touchRedis(term);
                evictOldestIfNeeded();
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable ({}), falling back to in-memory LRU cache", e.getMessage());
            }
        }
        localCache.put(term, result);
    }

    private void touchRedis(String term) {
        redisTemplate.opsForZSet().add(INDEX_KEY, term, Instant.now().toEpochMilli());
    }

    private void evictOldestIfNeeded() {
        Long size = redisTemplate.opsForZSet().zCard(INDEX_KEY);
        if (size == null || size <= properties.maxSize()) {
            return;
        }
        long excess = size - properties.maxSize();
        Set<Object> oldest = redisTemplate.opsForZSet().range(INDEX_KEY, 0, excess - 1);
        if (oldest != null) {
            for (Object key : oldest) {
                redisTemplate.delete(ENTRY_PREFIX + key);
                redisTemplate.opsForZSet().remove(INDEX_KEY, key);
            }
        }
    }

    private String normalize(String term) {
        return term == null ? "" : term.trim().toLowerCase();
    }

    /** Exposed for tests/metrics: current size of the in-memory fallback cache. */
    public int localCacheSize() {
        return localCache.size();
    }

    InMemoryLruCache<String, List<CustomerResponse>> localCache() {
        return localCache;
    }
}
