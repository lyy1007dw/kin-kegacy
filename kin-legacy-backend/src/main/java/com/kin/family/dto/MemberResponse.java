package com.kin.family.dto;

import com.kin.family.enums.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemberResponse {
    private Long id;
    private Long familyId;
    private String familyName;
    private Long userId;
    private String name;
    private Gender gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Integer isCreator;
    private List<MemberResponse> children;
    private LocalDateTime createTime;
}
