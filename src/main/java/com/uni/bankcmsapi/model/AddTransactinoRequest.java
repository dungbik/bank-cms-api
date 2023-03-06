package com.uni.bankcmsapi.model;

import lombok.Data;

@Data
public class AddTransactinoRequest {

    private int amount;
    private String bank;
    private String companyName;
    private String name;
    private int totalAmount;
    private String txTime;
    private String txType;
}
