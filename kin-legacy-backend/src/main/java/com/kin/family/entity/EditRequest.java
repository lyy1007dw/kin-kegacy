package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.constant.RequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 编辑申请实体
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edit_request")
public class EditRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long memberId;

    private Long applicantUserId;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private RequestStatusEnum status;

    private Long reviewerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime reviewedAt;
}
