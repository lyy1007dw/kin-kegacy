package com.kin.family.dto;

import com.kin.family.constant.RelationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RelationCreateDTO {
    @NotNull(message = "源成员ID不能为空")
    private Long fromMemberId;

    @NotNull(message = "目标成员ID不能为空")
    private Long toMemberId;

    @NotNull(message = "关系类型不能为空")
    private RelationTypeEnum relationType;
}
