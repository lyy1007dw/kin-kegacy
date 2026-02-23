package com.kin.family.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 成员关系类型枚举
 *
 * @author candong
 */
@Getter
public enum RelationTypeEnum {
    FATHER_SON("father_son", "父子"),
    MOTHER_SON("mother_son", "母女"),
    HUSBAND_WIFE("husband_wife", "夫妻"),
    SIBLING("sibling", "兄弟姐妹");

    @EnumValue
    private final String value;
    private final String description;

    RelationTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
