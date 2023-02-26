package com.uni.bankcmsapi.util;

import java.time.LocalDateTime;

public class KeyUtil {

    public static String makeDashboardKey(LocalDateTime dt, String companyName) {
        return (dt.getYear() * 10000 + dt.getMonth().getValue() * 100 + dt.getDayOfMonth()) + "_" + companyName;
    }
}
