package com.kin.family.dto;

import lombok.Data;

/**
 * 加入家谱请求DTO
 *
 * @author candong
 */
@Data
public class FamilyJoinDTO {
    private String code;
    private String name;
    private String relationDesc;
}
