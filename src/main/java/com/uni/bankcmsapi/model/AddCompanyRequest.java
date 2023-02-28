package com.uni.bankcmsapi.model;

import lombok.Data;

@Data
public class AddCompanyRequest {

    private String username;
    private String password;
    private String companyName;
    private double feeRate;
}
