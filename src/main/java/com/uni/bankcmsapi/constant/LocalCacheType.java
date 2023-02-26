package com.uni.bankcmsapi.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocalCacheType {

    M_USER("M_USER", 60 * 60, 1000),
    M_COMPANY("M_COMPANY", 60 * 60, 1000)
    ;

    private final String cacheName;
    private final int expireAfterWrite;
    private final int maximumSize;
}
