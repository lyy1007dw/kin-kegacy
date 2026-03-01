package com.kin.family.dto;

import com.kin.family.constant.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员关系DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRelationDTO {
    private Long memberId;
    private String memberName;
    private GenderEnum memberGender;
    private String relationType;
    private String relationLabel;
}
