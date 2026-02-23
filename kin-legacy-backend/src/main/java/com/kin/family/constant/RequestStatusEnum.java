package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author candong
 */
@Getter
public enum RequestStatusEnum {
    PENDING("pending", "待审批"),
    APPROVED("approved", "已同意"),
    REJECTED("rejected", "已拒绝");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    RequestStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
