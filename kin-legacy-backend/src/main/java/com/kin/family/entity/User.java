package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.constant.UserRoleEnum;
import com.kin.family.constant.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private UserRoleEnum role;

    private UserStatusEnum status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
