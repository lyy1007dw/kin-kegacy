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

    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private RequestStatus status;

    private Long reviewerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime reviewedAt;
}
