package com.uni.bankcmsapi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("H_TRANSACTION")
public class H_TRANSACTION {

    @Id private String id;
    private String companyName;
    private Bank bank;
    private TransactionType txType;
    private String name;
    private int amount;
    private int fee;
    private int balance;
    private int totalAmount;
    private LocalDateTime txTime;

    public enum Bank {
        KB,
        신협,
        광주,
        ;

    }

    public enum TransactionType {
        WITHDRAW,
        DEPOSIT
    }
}
