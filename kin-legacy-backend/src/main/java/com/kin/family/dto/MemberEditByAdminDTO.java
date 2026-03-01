package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberEditByAdminDTO {
    private String name;
    private GenderEnum gender;
    private LocalDate birthDate;
    private String birthPlace;
    private LocalDate deathDate;
    @Size(max = 500, message = "简介不能超过500个字符")
    private String bio;
    private String avatar;
    private Long genealogyId;
    private String accountRole;
}
