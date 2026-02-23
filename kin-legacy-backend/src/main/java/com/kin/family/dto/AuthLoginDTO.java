package com.kin.family.dto;

import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author candong
 */
@Data
public class AuthLoginDTO {
    private String username;
    private String password;
}
