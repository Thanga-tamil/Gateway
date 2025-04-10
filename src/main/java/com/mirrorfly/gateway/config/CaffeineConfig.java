package com.mirrorfly.gateway.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mirrorfly.gateway.model.ConnectionConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class CaffeineConfig {

    @Autowired
	private ApplicationProperties applicationProperties;

    @Bean
    public Cache<String, ConnectionConfig> connectionConfigCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Long.parseLong(applicationProperties.getCaffeineTtl()), TimeUnit.MINUTES) // Cache entries expire after 10 minutes
                .maximumSize(Long.parseLong(applicationProperties.getCaffeineTotalLimit())) // Maximum number of entries in the cache
                .removalListener(
                        (key, value, cause) -> log.info("Entry removed: key={}, value={}, cause={}", key, value, cause))
                .build();
    }
}