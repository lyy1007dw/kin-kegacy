package com.kin.family.dto;

import lombok.Data;

/**
 * 认证令牌DTO
 *
 * @author candong
 */
@Data
public class AuthTokenDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserDetailDTO userInfo;
}
