package com.kin.family.dto;

import com.kin.family.enums.UserRole;
import com.kin.family.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoResponse {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createTime;
}
