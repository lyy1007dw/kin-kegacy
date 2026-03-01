package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成员详情DTO
 *
 * @author candong
 */
@Data
public class MemberDetailDTO {
    private Long id;
    private Long familyId;
    private String familyName;
    private Long userId;
    private String name;
    private GenderEnum gender;
    private String avatar;
    private LocalDate birthDate;
    private String birthPlace;
    private LocalDate deathDate;
    private String bio;
    private Integer isCreator;
    private Integer generation;
    private Integer age;
    private String photos;
    private List<MemberRelationDTO> relations;
    private List<MemberDetailDTO> children;
    private LocalDateTime createTime;
}
