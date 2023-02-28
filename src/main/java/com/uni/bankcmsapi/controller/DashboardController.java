package com.uni.bankcmsapi.controller;

import com.uni.bankcmsapi.component.UserComponent;
import com.uni.bankcmsapi.entity.M_DASHBOARD;
import com.uni.bankcmsapi.entity.M_USER;
import com.uni.bankcmsapi.model.DashboardResponse;
import com.uni.bankcmsapi.repository.MDashboardRepository;
import com.uni.bankcmsapi.util.KeyUtil;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final MDashboardRepository mDashboardRepository;
    private final UserComponent userComponent;

    @GetMapping
    public DashboardResponse getDashboard(@RequestParam String companyName) {
        M_USER user = userComponent.getUser();
        if (user == null || StringUtils.isEmpty(companyName) || !user.getCompanyList().contains(companyName)) {
            log.warn("[getDashboard] user[{}] companyName[{}] user.getCompanyList()[{}]", user, companyName, user.getCompanyList());
            return new DashboardResponse();
        }

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);
        String todayKey = KeyUtil.makeDashboardKey(today, companyName);
        String yesterdayKey = KeyUtil.makeDashboardKey(yesterday, companyName);

        Map<String, M_DASHBOARD> mDashboardMap = mDashboardRepository.findAllById(List.of(yesterdayKey, todayKey)).stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e));
        DashboardResponse response = new DashboardResponse();
        if (mDashboardMap.containsKey(todayKey)) {
            response.setTotalDeposit(mDashboardMap.get(todayKey).getTotalDeposit());
            response.setTotalWithdraw(mDashboardMap.get(todayKey).getTotalWithdraw());
            response.setTotalFee(mDashboardMap.get(todayKey).getTotalFee());
            response.setTotalBalance(mDashboardMap.get(todayKey).getTotalBalance());
        }
        if (mDashboardMap.containsKey(yesterdayKey)) {
            response.setYTotalDeposit(mDashboardMap.get(yesterdayKey).getTotalDeposit());
            response.setYTotalWithdraw(mDashboardMap.get(yesterdayKey).getTotalWithdraw());
            response.setYTotalFee(mDashboardMap.get(yesterdayKey).getTotalFee());
            response.setYTotalBalance(mDashboardMap.get(yesterdayKey).getTotalBalance());
        }
        return response;
    }

}
