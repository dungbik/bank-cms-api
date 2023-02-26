package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_COMPANY;
import com.uni.bankcmsapi.entity.M_USER;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootTest
class MUserRepositoryTest {

    @Autowired private MUserRepository mUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void test() {
        this.mUserRepository.insert(new M_USER("test", passwordEncoder.encode("test"), List.of(M_COMPANY.Company.C1), List.of(M_USER.Authority.ROLE_ADMIN)));
    }

}