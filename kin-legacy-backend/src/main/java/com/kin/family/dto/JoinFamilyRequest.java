package com.kin.family.dto;

import lombok.Data;

@Data
public class JoinFamilyRequest {
    private String code;
    private String name;
    private String relationDesc;
}
