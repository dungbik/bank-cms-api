package com.uni.bankcmsapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodayDashboard {
    private String companyName;
    private long totalDeposit;
    private long totalWithdraw;
    private long totalFee;
    private long totalBalance;
}
