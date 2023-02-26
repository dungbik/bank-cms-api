package com.uni.bankcmsapi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("M_DASHBOARD")
public class M_DASHBOARD {

    @Id private String key; // date_companyName
    private long totalDeposit;
    private long totalWithdraw;
    private long totalFee;
    private long totalBalance;

}
