package com.kin.family.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationVO {
    private Long id;
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private String relationType;
    private String relationDesc;
}
