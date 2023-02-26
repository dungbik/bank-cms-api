package com.uni.bankcmsapi.model;

import com.uni.bankcmsapi.entity.M_COMPANY;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TodayDashboard {
    private M_COMPANY.Company companyName;
    private long totalDeposit;
    private long totalWithdraw;
    private long totalFee;
    private long totalBalance;
}
