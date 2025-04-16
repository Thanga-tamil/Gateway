package com.mirrorfly.gateway.cachemanager;

import com.mirrorfly.gateway.model.ConnectionConfig;

public interface CacheManager {

	void put(String key, ConnectionConfig value);

	ConnectionConfig get(String key);

	void invalidateAll();

	long size();

	ConnectionConfig getIfPresent(String key);

	String estimatedSize();

}