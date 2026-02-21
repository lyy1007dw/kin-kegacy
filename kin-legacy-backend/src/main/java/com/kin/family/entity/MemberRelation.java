package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.enums.RelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_relation")
public class MemberRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long fromMemberId;

    private Long toMemberId;

    @TableField(value = "relation_type", typeHandler = EnumTypeHandler.class)
    private RelationType relationType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
