package com.kin.family.service;

import com.kin.family.dto.RelationCreateDTO;
import com.kin.family.dto.RelationVO;

import java.util.List;

public interface MemberRelationService {
    RelationVO addRelation(Long familyId, RelationCreateDTO dto);

    List<RelationVO> getMemberRelations(Long familyId, Long memberId);

    void deleteRelation(Long familyId, Long relationId);
}
