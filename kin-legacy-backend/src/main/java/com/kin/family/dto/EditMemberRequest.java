package com.kin.family.dto;

import lombok.Data;

@Data
public class EditMemberRequest {
    private String fieldName;
    private String oldValue;
    private String newValue;
}
