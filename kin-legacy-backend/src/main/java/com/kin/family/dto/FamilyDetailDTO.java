package com.kin.family.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家谱详情DTO
 *
 * @author candong
 */
@Data
public class FamilyDetailDTO {
    private Long id;
    private String name;
    private String code;
    private String avatar;
    private String description;
    private Long creatorId;
    private Integer memberCount;
    private LocalDateTime createTime;
}
