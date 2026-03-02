package com.kin.family.dto;

import lombok.Data;

@Data
public class CreateFamilyRequest {
    private String name;
    private String description;
    private String avatar;
}
