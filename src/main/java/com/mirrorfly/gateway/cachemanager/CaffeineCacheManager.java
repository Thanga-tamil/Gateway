package com.mirrorfly.gateway.cachemanager;

import com.github.benmanes.caffeine.cache.Cache;
import com.mirrorfly.gateway.model.ConnectionConfig;

import org.springframework.stereotype.Component;

@Component
public class CaffeineCacheManager implements CacheManager {

    private final Cache<String, ConnectionConfig> cache;

    public CaffeineCacheManager(Cache<String, ConnectionConfig> cache) {
        this.cache = cache;
    }

    @Override
    public void put(String key, ConnectionConfig value) {
        cache.put(key, value);
    }

    @Override
    public ConnectionConfig get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override 
    public ConnectionConfig getIfPresent(String key) {
        return cache.getIfPresent(key);
    } 

    @Override
    public String estimatedSize() {
        return String.valueOf(cache.estimatedSize());
    }
}