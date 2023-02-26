package com.uni.bankcmsapi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("M_COMPANY")
public class M_COMPANY {

    @Id
    private Company companyName;
    private double feeRate;

    public enum Company {
        C1, C2, C3, C4, C5
    }

}
