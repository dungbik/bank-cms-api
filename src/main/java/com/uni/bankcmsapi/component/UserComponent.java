package com.uni.bankcmsapi.component;

import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.service.MstCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserComponent {

    private final MstCacheService mstCacheService;

    public M_USER getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return mstCacheService.findByUsername(authentication.getName());
        }

        return null;
    }

}
