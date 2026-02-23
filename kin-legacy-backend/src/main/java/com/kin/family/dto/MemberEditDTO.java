package com.kin.family.dto;

import lombok.Data;

/**
 * 成员编辑申请DTO
 *
 * @author candong
 */
@Data
public class MemberEditDTO {
    private String fieldName;
    private String oldValue;
    private String newValue;
}
