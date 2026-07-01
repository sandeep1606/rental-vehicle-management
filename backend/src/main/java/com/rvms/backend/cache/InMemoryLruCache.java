package com.rvms.backend.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Generic, thread-safe LRU cache backed by a LinkedHashMap in access-order mode.
 * Used as the Redis fallback so customer search caching still works without Redis running.
 */
public class InMemoryLruCache<K, V> {

    private final int maxSize;
    private final LinkedHashMap<K, V> store;
    private volatile BiConsumer<K, V> evictionListener;

    public InMemoryLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
        this.store = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > InMemoryLruCache.this.maxSize;
                if (shouldRemove && evictionListener != null) {
                    evictionListener.accept(eldest.getKey(), eldest.getValue());
                }
                return shouldRemove;
            }
        };
    }

    public synchronized V get(K key) {
        return store.get(key);
    }

    public synchronized void put(K key, V value) {
        store.put(key, value);
    }

    public synchronized boolean containsKey(K key) {
        return store.containsKey(key);
    }

    public synchronized int size() {
        return store.size();
    }

    public int maxSize() {
        return maxSize;
    }

    /** Returns keys ordered from least- to most-recently-used. Exposed for testing eviction order. */
    public synchronized List<K> keysInAccessOrder() {
        return new ArrayList<>(store.keySet());
    }

    public synchronized void clear() {
        store.clear();
    }

    public void setEvictionListener(BiConsumer<K, V> listener) {
        this.evictionListener = listener;
    }
}
