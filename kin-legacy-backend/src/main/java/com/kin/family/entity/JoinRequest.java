package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kin.family.enums.RequestStatus;
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
@TableName("join_request")
public class JoinRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long familyId;

    private Long applicantUserId;

    private String applicantName;

    private String relationDesc;

    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private RequestStatus status;

    private Long reviewerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime reviewedAt;
}
