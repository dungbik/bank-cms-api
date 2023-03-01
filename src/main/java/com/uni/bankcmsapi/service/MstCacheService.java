package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.entity.M_COMPANY;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.Company;
import com.uni.bankcmsapi.repository.MCompanyRepository;
import com.uni.bankcmsapi.repository.MUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MstCacheService {

    private final MUserRepository mUserRepository;
    private final MCompanyRepository mCompanyRepository;

    @Cacheable(cacheNames = "M_USER", key = "#username")
    public M_USER findByUsername(String username) {
        return mUserRepository.findByUsername(username).orElse(null);
    }

    @Cacheable(cacheNames = "M_COMPANY")
    public List<Company> getAllCompany() {
        return mCompanyRepository.findAll().stream()
                .map(e -> new Company(e.getCompanyName(), e.getFeeRate()))
                .collect(Collectors.toList());
    }

    @CacheEvict(cacheNames = "M_COMPANY")
    public void evictAllCompany() {}

}
