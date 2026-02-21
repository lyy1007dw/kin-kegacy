package com.kin.family.dto;

import com.kin.family.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalResponse {
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
    private RequestStatus status;
    private LocalDateTime createTime;
}
