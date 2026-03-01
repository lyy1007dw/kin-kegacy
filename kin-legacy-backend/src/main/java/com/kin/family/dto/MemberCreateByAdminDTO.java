package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员代用户创建成员DTO
 *
 * @author candong
 */
@Data
public class MemberCreateByAdminDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "家谱ID不能为空")
    private Long familyId;

    @NotNull(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String name;

    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别值不正确")
    private GenderEnum gender;

    @Size(max = 500, message = "头像URL不能超过500个字符")
    private String avatar;

    private LocalDate birthDate;

    @Size(max = 500, message = "简介不能超过500个字符")
    private String bio;

    private Long parentId;
}
