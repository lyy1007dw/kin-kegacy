package com.kin.family.dto;

import lombok.Data;

/**
 * 刷新令牌请求DTO
 *
 * @author candong
 */
@Data
public class AuthRefreshTokenDTO {
    private String refreshToken;
}
