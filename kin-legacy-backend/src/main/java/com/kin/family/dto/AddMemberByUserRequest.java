package com.kin.family.dto;

import com.kin.family.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddMemberByUserRequest {
    private Long userId;
    private Long familyId;
    private String name;
    private Gender gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Long parentId;
}
