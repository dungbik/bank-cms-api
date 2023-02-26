package com.uni.bankcmsapi.repository;

import com.uni.bankcmsapi.entity.M_MAIL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MMailRepositoryTest {

    @Autowired private MMailRepository mMailRepository;

    @Test
    void insertData() {
//        mMailRepository.insert(new M_MAIL("q1w2e30630@gmail.com", "xxydxdbwylchxxme", 0));
        mMailRepository.insert(new M_MAIL("dnjstjrj5@gmail.com", "lygrlbgbytzivgnf", 0));
    }

}