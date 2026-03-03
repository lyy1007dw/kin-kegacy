package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kin.family.dto.RelationCreateDTO;
import com.kin.family.dto.RelationVO;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.MemberRelation;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.MemberRelationMapper;
import com.kin.family.service.MemberRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRelationServiceImpl implements MemberRelationService {

    private final MemberRelationMapper relationMapper;
    private final FamilyMemberMapper memberMapper;

    @Override
    public RelationVO addRelation(Long familyId, RelationCreateDTO dto) {
        // 验证成员存在
        FamilyMember fromMember = memberMapper.selectById(dto.getFromMemberId());
        FamilyMember toMember = memberMapper.selectById(dto.getToMemberId());

        if (fromMember == null || toMember == null) {
            throw new BusinessException("成员不存在");
        }

        // 创建关系
        MemberRelation relation = MemberRelation.builder()
                .familyId(familyId)
                .fromMemberId(dto.getFromMemberId())
                .toMemberId(dto.getToMemberId())
                .relationType(dto.getRelationType())
                .build();
        relationMapper.insert(relation);

        return RelationVO.builder()
                .id(relation.getId())
                .fromMemberId(dto.getFromMemberId())
                .fromMemberName(fromMember.getName())
                .toMemberId(dto.getToMemberId())
                .toMemberName(toMember.getName())
                .relationType(dto.getRelationType().getValue())
                .relationDesc(dto.getRelationType().getDescription())
                .build();
    }

    @Override
    public List<RelationVO> getMemberRelations(Long familyId, Long memberId) {
        // 查询该成员的所有关系（作为from或to）
        List<MemberRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<MemberRelation>()
                        .eq(MemberRelation::getFamilyId, familyId)
                        .and(w -> w.eq(MemberRelation::getFromMemberId, memberId)
                                .or()
                                .eq(MemberRelation::getToMemberId, memberId))
        );

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有涉及的成员ID
        List<Long> memberIds = relations.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getFromMemberId(), r.getToMemberId()))
                .distinct()
                .collect(Collectors.toList());

        // 批量查询成员信息
        Map<Long, FamilyMember> memberMap = memberMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(FamilyMember::getId, m -> m));

        // 转换为VO
        List<RelationVO> result = new ArrayList<>();
        for (MemberRelation relation : relations) {
            FamilyMember fromMember = memberMap.get(relation.getFromMemberId());
            FamilyMember toMember = memberMap.get(relation.getToMemberId());

            RelationVO vo = RelationVO.builder()
                    .id(relation.getId())
                    .fromMemberId(relation.getFromMemberId())
                    .fromMemberName(fromMember != null ? fromMember.getName() : null)
                    .toMemberId(relation.getToMemberId())
                    .toMemberName(toMember != null ? toMember.getName() : null)
                    .relationType(relation.getRelationType().getValue())
                    .relationDesc(relation.getRelationType().getDescription())
                    .build();
            result.add(vo);
        }

        return result;
    }

    @Override
    public void deleteRelation(Long familyId, Long relationId) {
        MemberRelation relation = relationMapper.selectById(relationId);
        if (relation == null || !relation.getFamilyId().equals(familyId)) {
            throw new BusinessException("关系不存在");
        }
        relationMapper.deleteById(relationId);
    }
}
