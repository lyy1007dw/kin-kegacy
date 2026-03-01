package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO {
    private Long id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String birthPlace;
    private LocalDate deathDate;
    private String bio;
    private String avatar;
    private Long genealogyId;
    private String genealogyName;
    private Long userId;
    private String accountRole;
    private LocalDateTime createdAt;
}
