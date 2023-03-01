package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.component.TokenComponent;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.Company;
import com.uni.bankcmsapi.model.LoginRequest;
import com.uni.bankcmsapi.model.LoginResponse;
import com.uni.bankcmsapi.model.User;
import com.uni.bankcmsapi.service.MstCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final AuthenticationManagerBuilder managerBuilder;
    private final TokenComponent tokenComponent;
    private final MstCacheService mstCacheService;


    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());

        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
        String accessToken = tokenComponent.generateToken(authentication);

        M_USER mUser = mstCacheService.findByUsername(authentication.getName());

        final List<String> companyNameList = mUser.getCompanyList();
        List<Company> companyList;
        if (mUser.isAdmin()) {
            companyList = mstCacheService.getAllCompany();
        } else {
            companyList = mstCacheService.getAllCompany().stream()
                    .filter(e -> companyNameList.contains(e.getCompanyName()))
                    .collect(Collectors.toList());
        }

        return new LoginResponse(accessToken, new User(mUser.getUsername(), companyList, mUser.getAuthorities()));
    }
}
