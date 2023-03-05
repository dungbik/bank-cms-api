package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.component.UserComponent;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.Transaction;
import com.uni.bankcmsapi.model.TransactionResponse;
import com.uni.bankcmsapi.repository.HTransactionRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/txs")
public class TransactionController {

    private final HTransactionRepository hTransactionRepository;
    private final UserComponent userComponent;

    @GetMapping
    public TransactionResponse getTransaction(@RequestParam String companyName,
                                              @RequestParam String startDt,
                                              @RequestParam String endDt) {
        if (StringUtils.isEmpty(companyName) || StringUtils.isEmpty(startDt) || StringUtils.isEmpty(endDt)) {
            log.warn("[getTransaction] companyName[{}] startDt[{}] endDt[{}]", companyName, startDt, endDt);
            return new TransactionResponse();
        }

        M_USER user = userComponent.getUser();
        if (StringUtils.isEmpty(companyName) || !user.hasCompany(companyName)) {
            log.warn("[getTransaction] companyName[{}] user.getCompanyList()[{}]", companyName, user.getCompanyList());
            return new TransactionResponse();
        }

        List<Transaction> txs = hTransactionRepository.findTransaction(companyName, startDt, endDt);
        return new TransactionResponse(txs);
    }

}
