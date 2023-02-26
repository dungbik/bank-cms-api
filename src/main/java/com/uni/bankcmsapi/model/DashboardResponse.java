package com.uni.bankcmsapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DashboardResponse extends APIResponse {

    private long yTotalDeposit;
    private long yTotalWithdraw;
    private long yTotalFee;
    private long yTotalBalance;
    private long totalDeposit;
    private long totalWithdraw;
    private long totalFee;
    private long totalBalance;

}
