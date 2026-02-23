package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import lombok.Data;

import java.time.LocalDate;

/**
 * 成员创建DTO
 *
 * @author candong
 */
@Data
public class MemberCreateDTO {
    private String name;
    private GenderEnum gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Long parentId;
}
