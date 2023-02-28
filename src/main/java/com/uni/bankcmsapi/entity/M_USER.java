package com.uni.bankcmsapi.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("M_USER")
public class M_USER {

    @Id private String username;
    private String password;

    private List<String> companyList = new ArrayList<>();

    private List<Authority> authorities = new ArrayList<>();

    public enum Authority {
        ROLE_USER, ROLE_ADMIN
    }
}
