package com.uni.bankcmsapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/system")
public class SystemController {

    private final CacheManager cacheManager;

    @GetMapping("/cache/clear")
    public String clearCache() {
        this.cacheManager.getCacheNames().parallelStream()
                .forEach(e -> cacheManager.getCache(e).clear());

        return "ok";
    }

}
