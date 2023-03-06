package com.uni.bankcmsapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;
    private String companyName;
    private String bank;
    private String txType;
    private String name;
    private int amount;
    private int fee;
    private int totalAmount;
    private int balance;
    private LocalDateTime txTime;
}
