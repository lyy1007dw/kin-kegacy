package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户全局角色枚举
 *
 * @author candong
 */
@Getter
public enum UserRoleEnum {
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),
    GENEALOGY_ADMIN("GENEALOGY_ADMIN", "家谱管理员"),
    NORMAL_USER("NORMAL_USER", "普通用户");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    UserRoleEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == GENEALOGY_ADMIN;
    }

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
}
