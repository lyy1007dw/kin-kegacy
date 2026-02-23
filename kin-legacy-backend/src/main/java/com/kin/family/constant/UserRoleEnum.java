package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author candong
 */
@Getter
public enum UserRoleEnum {
    ADMIN("admin", "管理员"),
    USER("user", "普通用户");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    UserRoleEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
