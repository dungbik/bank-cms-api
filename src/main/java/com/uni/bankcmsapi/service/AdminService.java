package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.entity.M_COMPANY;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.APIResponse;
import com.uni.bankcmsapi.model.AddCompanyRequest;
import com.uni.bankcmsapi.repository.MCompanyRepository;
import com.uni.bankcmsapi.repository.MUserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final MstCacheService mstCacheService;
    private final MCompanyRepository mCompanyRepository;
    private final MUserRepository mUserRepository;
    private final PasswordEncoder passwordEncoder;

    public APIResponse addCompany(AddCompanyRequest param) {
        String companyName = param.getCompanyName();
        double feeRate = param.getFeeRate();

        String username = param.getUsername();
        String password = param.getPassword();

        if (StringUtils.isEmpty(companyName) || feeRate <= 0 || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            log.warn("[addCompany] Invalid param companyName[{}] feeRate[{}] username[{}] password[{}]", companyName, feeRate, username, password);
            return APIResponse.ofFail();
        }

        long count = mstCacheService.getAllCompany().stream()
                .filter(e -> e.getCompanyName().equals(companyName))
                .count();
        if (count > 0) {
            log.warn("[addCompany] Already exist companyName companyName[{}] feeRate[{}] username[{}] password[{}]", companyName, feeRate, username, password);
            return APIResponse.ofFail();
        }

        this.mCompanyRepository.insert(new M_COMPANY(companyName, feeRate));
        this.mUserRepository.insert(new M_USER(username, passwordEncoder.encode(password), List.of(companyName), List.of(M_USER.Authority.ROLE_USER)));

        this.mstCacheService.evictAllCompany();
        return null;
    }
}
