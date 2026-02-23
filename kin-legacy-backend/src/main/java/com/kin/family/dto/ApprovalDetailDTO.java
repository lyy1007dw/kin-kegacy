package com.kin.family.dto;

import com.kin.family.constant.RequestStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批详情DTO
 *
 * @author candong
 */
@Data
public class ApprovalDetailDTO {
    private Long id;
    private String type;
    private Long familyId;
    private Long applicantUserId;
    private String applicantName;
    private String relationDesc;
    private Long memberId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private RequestStatusEnum status;
    private LocalDateTime createTime;
}
