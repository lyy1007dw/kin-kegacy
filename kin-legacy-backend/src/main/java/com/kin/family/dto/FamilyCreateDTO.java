package com.kin.family.dto;

import lombok.Data;

/**
 * 创建家谱请求DTO
 *
 * @author candong
 */
@Data
public class FamilyCreateDTO {
    private String name;
    private String description;
    private String avatar;
}
