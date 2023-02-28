package com.uni.bankcmsapi.model;

import lombok.Data;

@Data
public class APIResponse {

    private boolean success = true;

    public static APIResponse ofFail() {
        APIResponse response = new APIResponse();
        response.setSuccess(false);
        return response;
    }
}
