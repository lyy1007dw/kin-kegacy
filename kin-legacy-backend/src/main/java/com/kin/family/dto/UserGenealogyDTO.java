package com.kin.family.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-家谱关联信息DTO
 *
 * @author candong
 */
@Data
public class UserGenealogyDTO {

    private Long genealogyId;

    private String genealogyName;

    private String role;

    private LocalDateTime joinedAt;
}
