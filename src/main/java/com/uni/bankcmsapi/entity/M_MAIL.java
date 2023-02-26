package com.uni.bankcmsapi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("M_MAIL")
public class M_MAIL {
    @Id private String email;
    private String password;
    private int lastNo;

    public void setLastNo(int lastNo) {
        this.lastNo = lastNo;
    }
}
