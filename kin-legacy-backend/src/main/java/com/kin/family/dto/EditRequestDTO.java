package com.kin.family.dto;

import com.kin.family.constant.RequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 编辑申请详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditRequestDTO {
    private Long id;
    private Long familyId;
    private String familyName;
    private Long memberId;
    private String memberName;
    private String applicantName;
    private String changesJson;
    private RequestStatusEnum status;
    private String rejectReason;
    private String reviewerName;
    private LocalDateTime createTime;
    private LocalDateTime reviewedAt;
}
