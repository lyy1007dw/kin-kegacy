package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 家谱内角色枚举
 *
 * @author candong
 */
@Getter
public enum GenealogyRoleEnum {
    ADMIN("ADMIN", "家谱管理员"),
    MEMBER("MEMBER", "普通成员");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    GenealogyRoleEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
