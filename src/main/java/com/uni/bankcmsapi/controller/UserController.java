package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.component.TokenComponent;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.LoginRequest;
import com.uni.bankcmsapi.model.LoginResponse;
import com.uni.bankcmsapi.model.User;
import com.uni.bankcmsapi.service.MstCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final AuthenticationManagerBuilder managerBuilder;
    private final TokenComponent tokenComponent;
    private final MstCacheService mUserCacheService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());

        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
        String accessToken = tokenComponent.generateToken(authentication);

        M_USER mUser = mUserCacheService.findByUsername(authentication.getName());
        return new LoginResponse(accessToken, new User(mUser.getUsername(), mUser.getCompanyList(), mUser.getAuthorities()));
    }
}
