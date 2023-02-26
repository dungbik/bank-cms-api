package com.uni.bankcmsapi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HTransactionRepositoryTest {

    @Autowired HTransactionRepository hTransactionRepository;

    @Test
    void test() {
//        System.out.println(hTransactionRepository.findTodayTransaction(PageRequest.of(0, 10)).getContent());
//        hTransactionRepository.save(new H_TRANSACTION(H_TRANSACTION.Bank.KB, H_TRANSACTION.TransactionType.DEPOSIT, "홍길동", 1000000, (int) (1000000 * 1.2D / 100), 1000000, LocalDateTime.now()));
//        hTransactionRepository.save(new H_TRANSACTION(H_TRANSACTION.Bank.KB, H_TRANSACTION.TransactionType.WITHDRAW, "홍길동", 1000000, 0, 0, LocalDateTime.now()));
    }
}