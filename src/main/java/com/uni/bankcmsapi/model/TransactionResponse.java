package com.uni.bankcmsapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionResponse extends APIResponse {

    private List<Transaction> txs;
}
