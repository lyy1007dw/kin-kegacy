package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.MemberCreateByAdminDTO;
import com.kin.family.dto.MemberCreateDTO;
import com.kin.family.dto.MemberEditDTO;
import com.kin.family.dto.MemberDetailDTO;
import com.kin.family.dto.PageResult;
import com.kin.family.vo.TreeNodeVO;
import com.kin.family.entity.EditRequest;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.MemberRelation;
import com.kin.family.entity.User;
import com.kin.family.constant.GenderEnum;
import com.kin.family.constant.RelationTypeEnum;
import com.kin.family.constant.RequestStatusEnum;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.EditRequestMapper;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.MemberRelationMapper;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成员服务实现
 *
 * @author candong
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final MemberRelationMapper relationMapper;
    private final EditRequestMapper editRequestMapper;
    private final UserMapper userMapper;

    @Override
    public List<MemberDetailDTO> getMembers(Long familyId) {
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

        Map<Long, List<MemberDetailDTO>> childrenMap = members.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.groupingBy(m -> {
                    for (MemberRelation r : relations) {
                        if (r.getToMemberId().equals(m.getId()) &&
                            (r.getRelationType() == RelationTypeEnum.FATHER_SON ||
                             r.getRelationType() == RelationTypeEnum.MOTHER_SON)) {
                            return r.getFromMemberId();
                        }
                    }
                    return 0L;
                }));

        return members.stream()
                .filter(m -> {
                    for (MemberRelation r : relations) {
                        if (r.getToMemberId().equals(m.getId()) &&
                            (r.getRelationType() == RelationTypeEnum.FATHER_SON ||
                             r.getRelationType() == RelationTypeEnum.MOTHER_SON)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(m -> {
                    MemberDetailDTO dto = convertToDetailDTO(m);
                    dto.setChildren(childrenMap.getOrDefault(m.getId(), new ArrayList<>()));
                    return dto;
                })
                .toList();
    }

    @Override
    public MemberDetailDTO getMemberById(Long familyId, Long memberId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        return convertToDetailDTO(member);
    }

    @Override
    @Transactional
    public MemberDetailDTO addMember(Long familyId, MemberCreateDTO request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能添加成员");
        }

        if (request.getUserId() != null) {
            User linkedUser = userMapper.selectById(request.getUserId());
            if (linkedUser != null && linkedUser.getName() != null && !linkedUser.getName().isEmpty()) {
                if (request.getName() != null && !request.getName().equals(linkedUser.getName())) {
                    throw new BusinessException("成员姓名必须与关联用户姓名一致");
                }
                request.setName(linkedUser.getName());
            }
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (request.getGender() == null) {
            throw new BusinessException("性别不能为空");
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .userId(request.getUserId())
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
                    .relationType(request.getGender() == GenderEnum.MALE ?
                            RelationTypeEnum.FATHER_SON : RelationTypeEnum.MOTHER_SON)
                    .build();
            relationMapper.insert(relation);
        }

        return convertToDetailDTO(member);
    }

    @Override
    @Transactional
    public void applyEditMember(Long familyId, Long memberId, MemberEditDTO request, Long userId) {
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
                .status(RequestStatusEnum.PENDING)
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
                .filter(r -> r.getRelationType() == RelationTypeEnum.HUSBAND_WIFE)
                .collect(Collectors.toMap(
                        MemberRelation::getFromMemberId,
                        MemberRelation::getToMemberId,
                        (v1, v2) -> v1
                ));

        for (MemberRelation r : relations) {
            TreeNodeVO fromNode = memberMap.get(r.getFromMemberId());
            TreeNodeVO toNode = memberMap.get(r.getToMemberId());
            if (fromNode == null || toNode == null) continue;

            if (r.getRelationType() == RelationTypeEnum.HUSBAND_WIFE) {
                if (fromNode.getSpouse() == null) {
                    fromNode.setSpouse(toNode);
                }
                if (toNode.getSpouse() == null) {
                    toNode.setSpouse(fromNode);
                }
            } else if (r.getRelationType() == RelationTypeEnum.FATHER_SON ||
                       r.getRelationType() == RelationTypeEnum.MOTHER_SON) {
                fromNode.getChildren().add(toNode);
            }
        }

        List<TreeNodeVO> rootNodes = members.stream()
                .filter(m -> !relations.stream()
                        .filter(r -> r.getRelationType() == RelationTypeEnum.FATHER_SON ||
                                     r.getRelationType() == RelationTypeEnum.MOTHER_SON)
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
    public MemberDetailDTO updateMember(Long familyId, Long memberId, MemberCreateDTO request, Long userId) {
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

        if (request.getName() != null && member.getUserId() != null) {
            User linkedUser = userMapper.selectById(member.getUserId());
            if (linkedUser != null && !request.getName().equals(linkedUser.getName())) {
                throw new BusinessException("该成员已关联用户账号，姓名不允许直接修改，请通过用户管理修改");
            }
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
        return convertToDetailDTO(member);
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

        LambdaQueryWrapper<MemberRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRelation::getFamilyId, familyId)
                .and(w -> w.eq(MemberRelation::getFromMemberId, memberId).or().eq(MemberRelation::getToMemberId, memberId));
        relationMapper.delete(wrapper);

        memberMapper.deleteById(memberId);
    }

    @Override
    public List<MemberDetailDTO> getAllMembers() {
        List<FamilyMember> members = memberMapper.selectList(null);

        List<Family> families = familyMapper.selectList(null);
        Map<Long, String> familyMap = families.stream()
                .collect(Collectors.toMap(Family::getId, Family::getName));

        return members.stream()
                .map(m -> {
                    MemberDetailDTO dto = convertToDetailDTO(m);
                    dto.setFamilyName(familyMap.get(m.getFamilyId()));
                    return dto;
                })
                .toList();
    }

    @Override
    public PageResult<MemberDetailDTO> getMembersPaged(Integer page, Integer size) {
        Page<FamilyMember> pageParam = new Page<>(page, size);
        IPage<FamilyMember> memberPage = memberMapper.selectPage(pageParam, null);

        List<Family> families = familyMapper.selectList(null);
        Map<Long, String> familyMap = families.stream()
                .collect(Collectors.toMap(Family::getId, Family::getName));

        List<MemberDetailDTO> records = memberPage.getRecords().stream()
                .map(m -> {
                    MemberDetailDTO dto = convertToDetailDTO(m);
                    dto.setFamilyName(familyMap.get(m.getFamilyId()));
                    return dto;
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

    private MemberDetailDTO convertToDetailDTO(FamilyMember member) {
        MemberDetailDTO dto = new MemberDetailDTO();
        dto.setId(member.getId());
        dto.setFamilyId(member.getFamilyId());
        dto.setUserId(member.getUserId());
        dto.setName(member.getName());
        dto.setGender(member.getGender());
        dto.setAvatar(member.getAvatar());
        dto.setBirthDate(member.getBirthDate());
        dto.setBio(member.getBio());
        dto.setIsCreator(member.getIsCreator());
        dto.setCreateTime(member.getCreateTime());
        return dto;
    }

    @Override
    public MemberDetailDTO addMemberByUser(Long familyId, Long userId, MemberCreateByAdminDTO request) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

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
                .gender(request.getGender() != null ? request.getGender() : GenderEnum.MALE)
                .avatar(request.getAvatar() != null ? request.getAvatar() : user.getAvatar())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .isCreator(0)
                .build();
        memberMapper.insert(member);

        return convertToDetailDTO(member);
    }
}
