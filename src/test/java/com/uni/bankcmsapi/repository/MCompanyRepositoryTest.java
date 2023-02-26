package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_COMPANY;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MCompanyRepositoryTest {
    @Autowired private MCompanyRepository mCompanyRepository;

    @Test
    void test() {
        mCompanyRepository.save(new M_COMPANY(M_COMPANY.Company.C1, 1.2));
    }
}