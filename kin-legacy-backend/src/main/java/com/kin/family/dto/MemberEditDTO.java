package com.kin.family.dto;

import lombok.Data;

import java.util.Map;

/**
 * 成员编辑申请DTO（多字段）
 *
 * @author candong
 */
@Data
public class MemberEditDTO {
    private Map<String, FieldChange> changes;

    @Data
    public static class FieldChange {
        private String oldValue;
        private String newValue;
    }
}
