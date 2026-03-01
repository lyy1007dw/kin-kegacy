package com.kin.family.dto;

import com.kin.family.constant.RequestStatusEnum;
import lombok.Data;

/**
 * 编辑申请查询DTO
 */
@Data
public class EditRequestQueryDTO {
    private Integer page = 1;
    private Integer size = 20;
    private Long familyId;
    private RequestStatusEnum status;
    private Long reviewerId;
}
