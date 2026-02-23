package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.constant.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家族成员实体
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("family_member")
public class FamilyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long userId;

    private String name;

    private GenderEnum gender;

    private String avatar;

    private LocalDate birthDate;

    private String bio;

    @TableField("is_creator")
    private Integer isCreator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
