package com.kin.family.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FamilyDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String avatar;
    private String description;
    private Long creatorId;
    private Integer memberCount;
    private LocalDateTime createTime;
}
