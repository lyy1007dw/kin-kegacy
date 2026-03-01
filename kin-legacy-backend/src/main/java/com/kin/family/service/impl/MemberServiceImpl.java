package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kin.family.dto.*;
import com.kin.family.vo.TreeNodeVO;
import com.kin.family.entity.*;
import com.kin.family.constant.*;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.*;
import com.kin.family.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
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
    private final UserGenealogyMapper userGenealogyMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public MemberDetailDTO getMemberDetail(Long familyId, Long memberId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getFamilyId().equals(familyId)) {
            throw new BusinessException("成员不存在");
        }

        MemberDetailDTO dto = convertToDetailDTO(member);
        
        if (member.getBirthDate() != null) {
            int age = Period.between(member.getBirthDate(), LocalDate.now()).getYears();
            dto.setAge(age);
        }
        
        dto.setRelations(getMemberRelations(memberId, familyId));
        
        dto.setPhotos("该功能待开发");
        
        return dto;
    }

    private List<MemberRelationDTO> getMemberRelations(Long memberId, Long familyId) {
        List<MemberRelationDTO> relations = new ArrayList<>();
        
        List<MemberRelation> memberRelations = relationMapper.selectList(
                new LambdaQueryWrapper<MemberRelation>()
                        .eq(MemberRelation::getFamilyId, familyId)
                        .and(w -> w.eq(MemberRelation::getFromMemberId, memberId).or().eq(MemberRelation::getToMemberId, memberId))
        );

        for (MemberRelation r : memberRelations) {
            Long relatedMemberId = r.getFromMemberId().equals(memberId) ? r.getToMemberId() : r.getFromMemberId();
            FamilyMember relatedMember = memberMapper.selectById(relatedMemberId);
            if (relatedMember != null) {
                MemberRelationDTO relationDTO = MemberRelationDTO.builder()
                        .memberId(relatedMember.getId())
                        .memberName(relatedMember.getName())
                        .memberGender(relatedMember.getGender())
                        .relationType(r.getRelationType().getValue())
                        .relationLabel(r.getRelationType().getDescription())
                        .build();
                relations.add(relationDTO);
            }
        }
        
        return relations;
    }

    @Override
    @Transactional
    public MemberDetailDTO addChildMember(Long familyId, Long parentId, MemberCreateDTO request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember parent = memberMapper.selectById(parentId);
        if (parent == null || !parent.getFamilyId().equals(familyId)) {
            throw new BusinessException("父/母成员不存在");
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (request.getGender() == null) {
            throw new BusinessException("性别不能为空");
        }

        FamilyMember child = FamilyMember.builder()
                .familyId(familyId)
                .userId(request.getUserId())
                .name(request.getName())
                .gender(request.getGender())
                .avatar(request.getAvatar())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .isCreator(0)
                .build();
        memberMapper.insert(child);

        RelationTypeEnum relationType = request.getGender() == GenderEnum.MALE ?
                RelationTypeEnum.FATHER_SON : RelationTypeEnum.MOTHER_SON;
        
        MemberRelation relation = MemberRelation.builder()
                .familyId(familyId)
                .fromMemberId(parentId)
                .toMemberId(child.getId())
                .relationType(relationType)
                .build();
        relationMapper.insert(relation);

        return convertToDetailDTO(child);
    }

    @Override
    @Transactional
    public MemberDetailDTO addParentMember(Long familyId, Long childId, MemberCreateDTO request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        FamilyMember child = memberMapper.selectById(childId);
        if (child == null || !child.getFamilyId().equals(familyId)) {
            throw new BusinessException("子/女成员不存在");
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (request.getGender() == null) {
            throw new BusinessException("性别不能为空");
        }

        FamilyMember parent = FamilyMember.builder()
                .familyId(familyId)
                .userId(request.getUserId())
                .name(request.getName())
                .gender(request.getGender())
                .avatar(request.getAvatar())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .isCreator(0)
                .build();
        memberMapper.insert(parent);

        RelationTypeEnum relationType = request.getGender() == GenderEnum.MALE ?
                RelationTypeEnum.FATHER_SON : RelationTypeEnum.MOTHER_SON;
        
        MemberRelation relation = MemberRelation.builder()
                .familyId(familyId)
                .fromMemberId(parent.getId())
                .toMemberId(childId)
                .relationType(relationType)
                .build();
        relationMapper.insert(relation);

        return convertToDetailDTO(parent);
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

        if (request.getChanges() == null || request.getChanges().isEmpty()) {
            throw new BusinessException("修改内容不能为空");
        }

        try {
            String changesJson = objectMapper.writeValueAsString(request.getChanges());
            
            EditRequest editRequest = EditRequest.builder()
                    .familyId(familyId)
                    .memberId(memberId)
                    .applicantUserId(userId)
                    .memberName(member.getName())
                    .changesJson(changesJson)
                    .status(RequestStatusEnum.PENDING)
                    .build();
            editRequestMapper.insert(editRequest);
        } catch (JsonProcessingException e) {
            throw new BusinessException("序列化修改内容失败");
        }
    }

    @Override
    public List<TreeNodeVO> getFamilyTree(Long familyId, Long currentUserId) {
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

        if (currentUserId != null) {
            markCurrentUser(rootNodes, family, currentUserId);
        }

        return rootNodes;
    }

    private void markCurrentUser(List<TreeNodeVO> nodes, Family family, Long currentUserId) {
        for (TreeNodeVO node : nodes) {
            if (currentUserId.equals(node.getUserId())) {
                String roleLabel = getRoleLabel(family, currentUserId);
                node.setCurrentUser(true);
                node.setCurrentUserLabel(roleLabel);
                break;
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markCurrentUser(node.getChildren(), family, currentUserId);
            }
        }
    }

    private String getRoleLabel(Family family, Long currentUserId) {
        if (family.getCreatorId().equals(currentUserId)) {
            return "创建者";
        }
        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(currentUserId, family.getId());
        if (ug != null && "ADMIN".equals(ug.getRole())) {
            return "管理员";
        }
        return "我";
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
                .userId(member.getUserId())
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
        dto.setBirthPlace(member.getBirthPlace());
        dto.setDeathDate(member.getDeathDate());
        dto.setBio(member.getBio());
        dto.setIsCreator(member.getIsCreator());
        dto.setCreateTime(member.getCreateTime());
        
        if (member.getBirthDate() != null) {
            int age = Period.between(member.getBirthDate(), LocalDate.now()).getYears();
            dto.setAge(age);
        }
        
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

    @Override
    public PageResult<MemberVO> queryMembers(MemberQueryRequest query) {
        Page<MemberVO> pageParam = new Page<>(query.getPage(), query.getSize());
        IPage<MemberVO> page = memberMapper.selectMemberPage(pageParam, query);
        return PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public MemberDetailDTO updateMemberByAdmin(Long memberId, MemberEditByAdminDTO request) {
        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        boolean isGenealogyChange = request.getGenealogyId() != null && !request.getGenealogyId().equals(member.getFamilyId());

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
        if (request.getBirthPlace() != null) {
            member.setBirthPlace(request.getBirthPlace());
        }
        if (request.getDeathDate() != null) {
            member.setDeathDate(request.getDeathDate());
        }

        if (isGenealogyChange) {
            Family targetFamily = familyMapper.selectById(request.getGenealogyId());
            if (targetFamily == null) {
                throw new BusinessException("目标家谱不存在");
            }

            LambdaQueryWrapper<MemberRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberRelation::getFamilyId, member.getFamilyId())
                    .and(w -> w.eq(MemberRelation::getFromMemberId, memberId).or().eq(MemberRelation::getToMemberId, memberId));
            relationMapper.delete(wrapper);

            member.setFamilyId(request.getGenealogyId());
        }

        memberMapper.updateById(member);

        if (request.getAccountRole() != null && member.getUserId() != null) {
            UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(member.getUserId(), member.getFamilyId());
            if (ug != null) {
                ug.setRole(GenealogyRoleEnum.valueOf(request.getAccountRole()));
                userGenealogyMapper.updateById(ug);
            }
        }

        return convertToDetailDTO(member);
    }

    @Override
    public MemberTransferCheckDTO checkMemberTransfer(Long memberId, Long targetGenealogyId) {
        FamilyMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        Family targetFamily = familyMapper.selectById(targetGenealogyId);
        if (targetFamily == null) {
            throw new BusinessException("目标家谱不存在");
        }

        LambdaQueryWrapper<MemberRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRelation::getFamilyId, member.getFamilyId())
                .and(w -> w.eq(MemberRelation::getFromMemberId, memberId).or().eq(MemberRelation::getToMemberId, memberId));
        long relationCount = relationMapper.selectCount(wrapper);

        List<String> warnings = new ArrayList<>();
        if (relationCount > 0) {
            warnings.add("将同时迁移该成员的关系数据（" + relationCount + "条关系）");
        }

        LambdaQueryWrapper<FamilyMember> sameNameWrapper = new LambdaQueryWrapper<>();
        sameNameWrapper.eq(FamilyMember::getFamilyId, targetGenealogyId)
                .eq(FamilyMember::getName, member.getName());
        long sameNameCount = memberMapper.selectCount(sameNameWrapper);
        if (sameNameCount > 0) {
            warnings.add("目标家谱存在同名成员");
        }

        return MemberTransferCheckDTO.builder()
                .canTransfer(true)
                .warnings(warnings)
                .affectedRelations((int) relationCount)
                .build();
    }
}
