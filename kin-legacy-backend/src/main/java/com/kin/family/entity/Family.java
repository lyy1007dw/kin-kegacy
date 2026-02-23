package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 家谱实体
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("family")
public class Family {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String avatar;

    private String description;

    @TableField("creator_id")
    private Long creatorId;

    private Integer memberCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
