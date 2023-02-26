package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.entity.M_USER;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MstCacheService mstCacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        M_USER mUser = mstCacheService.findByUsername(username);
        if (mUser == null) {
            throw new UsernameNotFoundException(username + " 을 DB에서 찾을 수 없습니다");
        }
        return this.createUserDetails(mUser);
    }

    private UserDetails createUserDetails(M_USER member) {
        return new User(
                member.getUsername(),
                member.getPassword(),
                member.getAuthorities().stream()
                        .map(e -> new SimpleGrantedAuthority(e.name()))
                        .collect(Collectors.toList())
        );
    }
}