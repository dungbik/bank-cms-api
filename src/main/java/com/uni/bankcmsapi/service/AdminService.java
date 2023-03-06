package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.entity.H_TRANSACTION.Bank;
import com.uni.bankcmsapi.entity.H_TRANSACTION.TransactionType;
import com.uni.bankcmsapi.entity.M_COMPANY;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.APIResponse;
import com.uni.bankcmsapi.model.AddCompanyRequest;
import com.uni.bankcmsapi.model.AddTransactinoRequest;
import com.uni.bankcmsapi.model.Company;
import com.uni.bankcmsapi.repository.HTransactionRepository;
import com.uni.bankcmsapi.repository.MCompanyRepository;
import com.uni.bankcmsapi.repository.MUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final MstCacheService mstCacheService;
    private final MCompanyRepository mCompanyRepository;
    private final MUserRepository mUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final HTransactionRepository hTransactionRepository;

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

        return new APIResponse();
    }

    public APIResponse addTransaction(AddTransactinoRequest param) {
        int amount = param.getAmount();
        Bank bank = Bank.valueOf(param.getBank());
        String companyName = param.getCompanyName();
        String name = param.getName();
        int totalAmount = param.getTotalAmount();
        String txTime = param.getTxTime();
        TransactionType txType = TransactionType.valueOf(param.getTxType());

        if (amount <= 0 || bank == null || StringUtils.isEmpty(companyName) || StringUtils.isEmpty(name)
                || totalAmount <= 0 || StringUtils.isEmpty(txTime) || txType == null) {
            log.warn("[addTransaction] Invalid param amount[{}] bank[{}] companyName[{}] name[{}] totalAmount[{}] txTime[{}] txType[{}]",
                    amount, bank, companyName, name, totalAmount, txTime, txType);
            return APIResponse.ofFail();
        }

        Company company = mstCacheService.getAllCompany().stream()
                .filter(e -> e.getCompanyName().equals(companyName))
                .findFirst()
                .orElse(null);
        if (company == null) {
            log.warn("[addTransaction] company is null companyName[{}]",
                    companyName);
            return APIResponse.ofFail();
        }

        int fee = 0;
        int balance = 0;
        if (txType.equals(TransactionType.DEPOSIT)) {
            fee = (int) (amount * company.getFeeRate() / 100);
            balance = amount - fee;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dt = LocalDateTime.parse(txTime, formatter);

        H_TRANSACTION hTransaction = new H_TRANSACTION(
                null, companyName, bank, txType,
                name, amount, fee, balance, totalAmount, dt);

        this.hTransactionRepository.insert(hTransaction);

        return new APIResponse();
    }

    public APIResponse deleteTransaction(String id) {

        this.hTransactionRepository.deleteById(id);

        return new APIResponse();
    }
}
