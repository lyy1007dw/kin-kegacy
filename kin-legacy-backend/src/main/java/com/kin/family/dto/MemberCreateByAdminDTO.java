package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员代用户创建成员DTO
 *
 * @author candong
 */
@Data
public class MemberCreateByAdminDTO {
    private Long userId;
    private Long familyId;
    private String name;
    private GenderEnum gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Long parentId;
}
