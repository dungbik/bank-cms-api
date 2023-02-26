package com.uni.bankcmsapi.model;

import com.uni.bankcmsapi.entity.M_USER;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private String username;
    private List<String> companyList;
    private List<M_USER.Authority> authority;
}
