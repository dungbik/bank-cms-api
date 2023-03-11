package com.uni.bankcmsapi.service;

import com.uni.bankcmsapi.entity.H_TRANSACTION;
import com.uni.bankcmsapi.entity.M_DASHBOARD;
import com.uni.bankcmsapi.model.Company;
import com.uni.bankcmsapi.model.TodayDashboard;
import com.uni.bankcmsapi.model.Transaction;
import com.uni.bankcmsapi.repository.HTransactionRepository;
import com.uni.bankcmsapi.repository.MDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {

    private final MstCacheService mstCacheService;
    private final HTransactionRepository hTransactionRepository;
    private final NotificationService notificationService;
    private final MDashboardRepository mDashboardRepository;

    public void addTransaction(String msg) {

        Map<String, Company> companyMap = this.mstCacheService.getAllCompany().stream()
                .collect(Collectors.toMap(e -> e.getCompanyName(), e -> e));

        String[] splitMessage = msg.split("\n");
        String companyName = splitMessage[0].trim();

        int startIndex = 1;

        H_TRANSACTION.Bank bank = null;
        for (H_TRANSACTION.Bank b : H_TRANSACTION.Bank.values()) {
            if (splitMessage[1].contains(b.name())) {
                bank = b;
                break;
            }
        }

        if (bank == null) {
            if (splitMessage[splitMessage.length - 1].contains(H_TRANSACTION.Bank.기업.name())) {
                bank = H_TRANSACTION.Bank.기업;
            }
        }

        if (bank == null) {
            for (H_TRANSACTION.Bank b : H_TRANSACTION.Bank.values()) {
                if (splitMessage[2].contains(b.name())) {
                    startIndex = 2;
                    bank = b;
                    break;
                }
            }
        }

        if (bank == null) {
            log.error("[addTransaction] unknown bank companyName[{}] msg[{}]", companyName, msg);
            return;
        }

        Company company = companyMap.get(companyName);
        if (company == null) {
            log.error("[addTransaction] company is null companyName[{}] msg[{}]", companyName, msg);
            return;
        }

        String name = null;
        boolean isDeposit = false;
        int amount = 0;
        int fee = 0;
        int balance = 0;
        int totalAmount = 0;
        String dateTimeStr = null;

        if (bank.equals(H_TRANSACTION.Bank.KB)) {
            name = splitMessage[startIndex + 2];
            isDeposit = splitMessage[startIndex + 3].contains("입금");
            amount = Integer.parseInt(splitMessage[startIndex + 4].replaceAll("[^0-9]", ""));

            totalAmount = Integer.parseInt(splitMessage[startIndex + 5].replaceAll("[^0-9]", ""));
            dateTimeStr = LocalDateTime.now().getYear() + "/" + splitMessage[startIndex].substring(4, 15);
        } else if (bank.equals(H_TRANSACTION.Bank.신협)) {
            String[] firstLineSplit = splitMessage[startIndex].split(" ");

            String dateStr = firstLineSplit[1];
            String timeStr = firstLineSplit[2];
            isDeposit = firstLineSplit[3].contains("입금");
            dateTimeStr = LocalDateTime.now().getYear() + "/" + dateStr + " " + timeStr;

            amount = Integer.parseInt(firstLineSplit[4].replaceAll("[^0-9]", ""));
            name = firstLineSplit[5];
            totalAmount = Integer.parseInt(firstLineSplit[6].replaceAll("[^0-9]", ""));
        } else if (bank.equals(H_TRANSACTION.Bank.광주)) {
            isDeposit = splitMessage[startIndex + 2].contains("입금");
            dateTimeStr = LocalDateTime.now().getYear() + "/" + splitMessage[startIndex + 1].trim();

            amount = Integer.parseInt(splitMessage[startIndex + 2].replaceAll("[^0-9]", ""));
            name = splitMessage[startIndex + 4];
            totalAmount = Integer.parseInt(splitMessage[startIndex + 3].replaceAll("[^0-9]", ""));
        } else if (bank.equals(H_TRANSACTION.Bank.기업)) {
            dateTimeStr = splitMessage[startIndex].trim();
            isDeposit = splitMessage[startIndex + 1].contains("입금");
            amount = Integer.parseInt(splitMessage[startIndex + 1].replaceAll("[^0-9]", ""));
            totalAmount = Integer.parseInt(splitMessage[startIndex + 2].replaceAll("[^0-9]", ""));
            name = splitMessage[startIndex + 3];
        }

        if (isDeposit) {
            fee = (int) (amount * company.getFeeRate() / 100);
            balance = amount - fee;
        }

        if (name == null || dateTimeStr == null) {
            log.error("[addTransaction] parse error companyName[{}] name[{}] dateTimeStr[{}] msg[{}]", companyName, name, dateTimeStr, msg);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime dt = LocalDateTime.now();
        try {
            dt = LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception ex) {
            log.error("[addTransaction] date parse error dateTimeStr[{}]", dateTimeStr);
        }

        H_TRANSACTION hTransaction = new H_TRANSACTION(
                null, companyName, bank,
                isDeposit ? H_TRANSACTION.TransactionType.DEPOSIT : H_TRANSACTION.TransactionType.WITHDRAW,
                name, amount, fee, balance, totalAmount, dt);

        this.hTransactionRepository.insert(hTransaction);

        Transaction tx = new Transaction(hTransaction.getId(), companyName, hTransaction.getBank().name(), hTransaction.getTxType().name(), hTransaction.getName(), hTransaction.getAmount(), hTransaction.getFee(), hTransaction.getTotalAmount(), hTransaction.getBalance(), hTransaction.getTxTime());
        if (tx != null) {
            this.notificationService.sendAll("tx", tx);
        }

        String key = (dt.getYear() * 10000 + dt.getMonth().getValue() * 100 + dt.getDayOfMonth()) + "_" + companyName;
        this.mDashboardRepository.updateDashboard(key, isDeposit ? amount : 0, isDeposit ? 0 : amount, fee, balance);


        M_DASHBOARD mDashboard = this.mDashboardRepository.findById(key).orElse(null);
        if (mDashboard != null) {
            TodayDashboard todayDashboard = new TodayDashboard(companyName, mDashboard.getTotalDeposit(), mDashboard.getTotalWithdraw(), mDashboard.getTotalFee(), mDashboard.getTotalBalance());
            this.notificationService.sendAll("dashboard", todayDashboard);
        }

    }
}
