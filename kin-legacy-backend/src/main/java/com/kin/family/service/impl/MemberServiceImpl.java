package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.AddMemberByUserRequest;
import com.kin.family.dto.AddMemberRequest;
import com.kin.family.dto.EditMemberRequest;
import com.kin.family.dto.MemberResponse;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.TreeNodeVO;
import com.kin.family.entity.EditRequest;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.MemberRelation;
import com.kin.family.entity.User;
import com.kin.family.enums.Gender;
import com.kin.family.enums.RelationType;
import com.kin.family.enums.RequestStatus;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.EditRequestMapper;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.MemberRelationMapper;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.MemberService;
import com.kin.family.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final MemberRelationMapper relationMapper;
    private final EditRequestMapper editRequestMapper;
    private final UserMapper userMapper;

    @Override
    public List<MemberResponse> getMembers(Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        List<FamilyMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
                        .orderByAsc(FamilyMember::getId)
        );

        List<MemberRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<MemberRelation>()
                        .eq(MemberRelation::getFamilyId, familyId)
        );

        Map<Long, List<MemberResponse>> childrenMap = members.stream()
                .map(this::convertToResponse)
                .collect(Collectors.groupingBy(m -> {
                    for (MemberRelation r : relations) {
                        if (r.getToMemberId().equals(m.getId()) && 
                            (r.getRelationType() == RelationType.father_son || 
                             r.getRelationType() == RelationType.mother_son)) {
                            return r.getFromMemberId();
                        }
                    }
                    return 0L;
                }));

        return members.stream()
                .filter(m -> {
                    for (MemberRelation r : relations) {
                        if (r.getToMemberId().equals(m.getId()) && 
                            (r.getRelationType() == RelationType.father_son || 
                             r.getRelationType() == RelationType.mother_son)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(m -> {
                    MemberResponse response = convertToResponse(m);
                    response.setChildren(childrenMap.getOrDefault(m.getId(), new ArrayList<>()));
                    return response;
                })
                .toList();
    }

    @Override
    public MemberResponse getMemberById(Long familyId, Long memberId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        return convertToResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse addMember(Long familyId, AddMemberRequest request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能添加成员");
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (request.getGender() == null) {
            throw new BusinessException("性别不能为空");
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .name(request.getName())
                .gender(request.getGender())
                .avatar(request.getAvatar())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .isCreator(0)
                .build();
        memberMapper.insert(member);

        if (request.getParentId() != null) {
            MemberRelation relation = MemberRelation.builder()
                    .familyId(familyId)
                    .fromMemberId(request.getParentId())
                    .toMemberId(member.getId())
                    .relationType(request.getGender() == Gender.male ? 
                            RelationType.father_son : RelationType.mother_son)
                    .build();
            relationMapper.insert(relation);
        }

        return convertToResponse(member);
    }

    @Override
    @Transactional
    public void applyEditMember(Long familyId, Long memberId, EditMemberRequest request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        if (request.getFieldName() == null || request.getFieldName().isEmpty()) {
            throw new BusinessException("修改字段不能为空");
        }
        if (request.getNewValue() == null || request.getNewValue().isEmpty()) {
            throw new BusinessException("新值不能为空");
        }

        String oldValue = switch (request.getFieldName()) {
            case "name" -> member.getName();
            case "avatar" -> member.getAvatar();
            case "birthDate" -> member.getBirthDate() != null ? 
                    member.getBirthDate().toString() : null;
            case "bio" -> member.getBio();
            default -> null;
        };

        EditRequest editRequest = EditRequest.builder()
                .familyId(familyId)
                .memberId(memberId)
                .applicantUserId(userId)
                .fieldName(request.getFieldName())
                .oldValue(oldValue)
                .newValue(request.getNewValue())
                .status(RequestStatus.pending)
                .build();
        editRequestMapper.insert(editRequest);
    }

    @Override
    public List<TreeNodeVO> getFamilyTree(Long familyId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        List<FamilyMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>()
                        .eq(FamilyMember::getFamilyId, familyId)
        );

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        List<MemberRelation> relations = relationMapper.selectList(
                new LambdaQueryWrapper<MemberRelation>()
                        .eq(MemberRelation::getFamilyId, familyId)
        );

        Map<Long, TreeNodeVO> memberMap = members.stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toMap(TreeNodeVO::getId, v -> v));

        Map<Long, Long> spouseMap = relations.stream()
                .filter(r -> r.getRelationType() == RelationType.husband_wife)
                .collect(Collectors.toMap(
                        MemberRelation::getFromMemberId,
                        MemberRelation::getToMemberId,
                        (v1, v2) -> v1
                ));

        for (MemberRelation r : relations) {
            TreeNodeVO fromNode = memberMap.get(r.getFromMemberId());
            TreeNodeVO toNode = memberMap.get(r.getToMemberId());
            if (fromNode == null || toNode == null) continue;

            if (r.getRelationType() == RelationType.husband_wife) {
                if (fromNode.getSpouse() == null) {
                    fromNode.setSpouse(toNode);
                }
                if (toNode.getSpouse() == null) {
                    toNode.setSpouse(fromNode);
                }
            } else if (r.getRelationType() == RelationType.father_son || 
                       r.getRelationType() == RelationType.mother_son) {
                fromNode.getChildren().add(toNode);
            }
        }

        List<TreeNodeVO> rootNodes = members.stream()
                .filter(m -> !relations.stream()
                        .filter(r -> r.getRelationType() == RelationType.father_son || 
                                     r.getRelationType() == RelationType.mother_son)
                        .map(MemberRelation::getToMemberId)
                        .collect(Collectors.toSet())
                        .contains(m.getId()))
                .map(m -> memberMap.get(m.getId()))
                .filter(node -> node != null)
                .sorted((a, b) -> {
                    if (Boolean.TRUE.equals(a.getIsCreator() == 1)) return -1;
                    if (Boolean.TRUE.equals(b.getIsCreator() == 1)) return 1;
                    return 0;
                })
                .toList();

        return rootNodes;
    }

    @Override
    public MemberResponse updateMember(Long familyId, Long memberId, AddMemberRequest request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能编辑成员");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getAvatar() != null) {
            member.setAvatar(request.getAvatar());
        }
        if (request.getBirthDate() != null) {
            member.setBirthDate(request.getBirthDate());
        }
        if (request.getBio() != null) {
            member.setBio(request.getBio());
        }

        memberMapper.updateById(member);
        return convertToResponse(member);
    }

    @Override
    public void deleteMember(Long familyId, Long memberId, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能删除成员");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        if (member.getIsCreator() == 1) {
            throw new BusinessException("不能删除创建者");
        }

        // 删除关联关系
        LambdaQueryWrapper<MemberRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRelation::getFamilyId, familyId)
                .and(w -> w.eq(MemberRelation::getFromMemberId, memberId).or().eq(MemberRelation::getToMemberId, memberId));
        relationMapper.delete(wrapper);

        // 删除成员
        memberMapper.deleteById(memberId);
    }

    @Override
    public List<MemberResponse> getAllMembers() {
        List<FamilyMember> members = memberMapper.selectList(null);
        
        // 获取所有家谱信息
        List<Family> families = familyMapper.selectList(null);
        Map<Long, String> familyMap = families.stream()
                .collect(Collectors.toMap(Family::getId, Family::getName));
        
        return members.stream()
                .map(m -> {
                    MemberResponse response = convertToResponse(m);
                    response.setFamilyName(familyMap.get(m.getFamilyId()));
                    return response;
                })
                .toList();
    }

    @Override
    public PageResult<MemberResponse> getMembersPaged(Integer page, Integer size) {
        Page<FamilyMember> pageParam = new Page<>(page, size);
        IPage<FamilyMember> memberPage = memberMapper.selectPage(pageParam, null);
        
        List<Family> families = familyMapper.selectList(null);
        Map<Long, String> familyMap = families.stream()
                .collect(Collectors.toMap(Family::getId, Family::getName));
        
        List<MemberResponse> records = memberPage.getRecords().stream()
                .map(m -> {
                    MemberResponse response = convertToResponse(m);
                    response.setFamilyName(familyMap.get(m.getFamilyId()));
                    return response;
                })
                .toList();
        
        return PageResult.of(records, memberPage.getTotal(), page, size);
    }

    private TreeNodeVO convertToTreeNode(FamilyMember member) {
        return TreeNodeVO.builder()
                .id(member.getId())
                .name(member.getName())
                .gender(member.getGender())
                .avatar(member.getAvatar())
                .birthDate(member.getBirthDate())
                .bio(member.getBio())
                .isCreator(member.getIsCreator())
                .children(new ArrayList<>())
                .build();
    }

    private MemberResponse convertToResponse(FamilyMember member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFamilyId(member.getFamilyId());
        response.setUserId(member.getUserId());
        response.setName(member.getName());
        response.setGender(member.getGender());
        response.setAvatar(member.getAvatar());
        response.setBirthDate(member.getBirthDate());
        response.setBio(member.getBio());
        response.setIsCreator(member.getIsCreator());
        response.setCreateTime(member.getCreateTime());
        return response;
    }

    @Override
    public MemberResponse addMemberByUser(Long familyId, Long userId, AddMemberByUserRequest request) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户是否已是该家谱成员
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId);
        long count = memberMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该用户已是家谱成员");
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .userId(userId)
                .name(request.getName() != null ? request.getName() : user.getNickname())
                .gender(request.getGender() != null ? request.getGender() : Gender.male)
                .avatar(request.getAvatar() != null ? request.getAvatar() : user.getAvatar())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .isCreator(0)
                .build();
        memberMapper.insert(member);

        return convertToResponse(member);
    }
}
