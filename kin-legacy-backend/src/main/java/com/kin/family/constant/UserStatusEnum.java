package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author candong
 */
@Getter
public enum UserStatusEnum {
    NORMAL("normal"),
    DISABLED("disabled");

    @EnumValue
    @JsonValue
    private final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }
}
