package com.uni.bankcmsapi.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParseMailSchedulerTest {

    @Autowired ParseMailScheduler parseMailScheduler;

    @Test
    void execute() throws Exception {
        this.parseMailScheduler.execute();
    }
}