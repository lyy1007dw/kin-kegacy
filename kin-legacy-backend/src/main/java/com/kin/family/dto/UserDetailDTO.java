package com.kin.family.dto;

import com.kin.family.constant.UserRoleEnum;
import com.kin.family.constant.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户详情DTO
 *
 * @author candong
 */
@Data
public class UserDetailDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private UserRoleEnum role;
    private UserStatusEnum status;
    private LocalDateTime createTime;
}
