package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.constant.RequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 加入申请实体
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("join_request")
public class JoinRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long applicantUserId;

    private String applicantName;

    private String relationDesc;

    private RequestStatusEnum status;

    private Long reviewerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime reviewedAt;
}
