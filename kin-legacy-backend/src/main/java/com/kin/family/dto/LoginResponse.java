package com.kin.family.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfoResponse user;
}
