package com.uni.bankcmsapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.uni.bankcmsapi.constant.LocalCacheType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public List<CaffeineCache> caffeineCaches() {
        return Arrays.stream(LocalCacheType.values())
                .map(cache -> new CaffeineCache(cache.getCacheName(), Caffeine.newBuilder()
                                .recordStats()
                                .expireAfterWrite(cache.getExpireAfterWrite(),
                                        TimeUnit.SECONDS)
                                .maximumSize(cache.getMaximumSize())
                                .build()
                        )
                )
                .toList();
    }

    @Bean
    public CacheManager cacheManager() {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caffeineCaches());
        return cacheManager;
    }
}
