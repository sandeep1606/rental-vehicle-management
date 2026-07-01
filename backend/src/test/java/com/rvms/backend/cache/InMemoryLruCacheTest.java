package com.rvms.backend.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLruCacheTest {

    private InMemoryLruCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryLruCache<>(3);
    }

    @Test
    void evictsLeastRecentlyUsedEntryWhenCapacityExceeded() {
        cache.put("a", "Alice");
        cache.put("b", "Bob");
        cache.put("c", "Carol");

        // capacity 3 is full; inserting a 4th entry should evict "a" (least recently used)
        cache.put("d", "Dave");

        assertThat(cache.size()).isEqualTo(3);
        assertThat(cache.containsKey("a")).isFalse();
        assertThat(cache.containsKey("b")).isTrue();
        assertThat(cache.containsKey("c")).isTrue();
        assertThat(cache.containsKey("d")).isTrue();
    }

    @Test
    void accessingAnEntryProtectsItFromEviction() {
        cache.put("a", "Alice");
        cache.put("b", "Bob");
        cache.put("c", "Carol");

        // touch "a" so "b" becomes the least recently used instead
        cache.get("a");
        cache.put("d", "Dave");

        assertThat(cache.containsKey("a")).isTrue();
        assertThat(cache.containsKey("b")).isFalse();
        assertThat(cache.containsKey("c")).isTrue();
        assertThat(cache.containsKey("d")).isTrue();
    }

    @Test
    void keysInAccessOrderReflectsRecencyOldestFirst() {
        cache.put("a", "Alice");
        cache.put("b", "Bob");
        cache.put("c", "Carol");
        cache.get("a"); // "a" is now most-recently-used

        assertThat(cache.keysInAccessOrder()).containsExactly("b", "c", "a");
    }

    @Test
    void evictionListenerIsInvokedWithTheEvictedEntry() {
        AtomicInteger evictedCount = new AtomicInteger(0);
        List<String> evictedKeys = new java.util.ArrayList<>();
        cache.setEvictionListener((key, value) -> {
            evictedCount.incrementAndGet();
            evictedKeys.add(key);
        });

        cache.put("a", "Alice");
        cache.put("b", "Bob");
        cache.put("c", "Carol");
        cache.put("d", "Dave"); // evicts "a"

        assertThat(evictedCount.get()).isEqualTo(1);
        assertThat(evictedKeys).containsExactly("a");
    }

    @Test
    void rejectsNonPositiveMaxSize() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new InMemoryLruCache<String, String>(0));
    }
}
