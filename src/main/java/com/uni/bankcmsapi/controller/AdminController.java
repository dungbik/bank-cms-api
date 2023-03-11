package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.model.APIResponse;
import com.uni.bankcmsapi.model.AddCompanyRequest;
import com.uni.bankcmsapi.model.AddTransactionRequest;
import com.uni.bankcmsapi.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/company")
    public APIResponse addCompany(@RequestBody AddCompanyRequest param) {
        return this.adminService.addCompany(param);
    }

    @PostMapping("/add-data")
    public APIResponse addTransaction(@RequestBody AddTransactionRequest param) {
        return this.adminService.addTransaction(param);
    }

    @DeleteMapping("/delete-data")
    public APIResponse addTransaction(@RequestParam String id) {
        return this.adminService.deleteTransaction(id);
    }

}
