package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.model.APIResponse;
import com.uni.bankcmsapi.model.AddCompanyRequest;
import com.uni.bankcmsapi.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/company")
    public APIResponse addCompany(@RequestBody AddCompanyRequest param) {
        return this.adminService.addCompany(param);
    }
}
