package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.constant.RelationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成员关系实体
 *
 * @author candong
 */
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

    private RelationTypeEnum relationType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
