package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author candong
 */
@Getter
public enum GenderEnum {
    MALE("male", "男"),
    FEMALE("female", "女");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    GenderEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
