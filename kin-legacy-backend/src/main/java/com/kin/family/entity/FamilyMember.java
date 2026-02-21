package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @TableField(value = "gender", typeHandler = EnumTypeHandler.class)
    private Gender gender;

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
