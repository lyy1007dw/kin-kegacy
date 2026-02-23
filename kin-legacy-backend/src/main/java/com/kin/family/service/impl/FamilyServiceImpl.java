package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.FamilyCreateDTO;
import com.kin.family.dto.FamilyDetailDTO;
import com.kin.family.dto.FamilyJoinDTO;
import com.kin.family.dto.PageResult;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.JoinRequest;
import com.kin.family.entity.User;
import com.kin.family.constant.GenderEnum;
import com.kin.family.constant.RequestStatusEnum;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.JoinRequestMapper;
import com.kin.family.mapper.UserMapper;
import com.kin.family.mapper.UserGenealogyMapper;
import com.kin.family.service.FamilyService;
import com.kin.family.service.UserRoleService;
import com.kin.family.util.FamilyCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 家谱服务实现
 *
 * @author candong
 */
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final JoinRequestMapper joinRequestMapper;
    private final UserMapper userMapper;
    private final UserGenealogyMapper userGenealogyMapper;
    private final UserRoleService userRoleService;

    @Override
    @Transactional
    public FamilyDetailDTO createFamily(FamilyCreateDTO request, Long userId) {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("家谱名称不能为空");
        }

        String code;
        do {
            code = FamilyCodeUtil.generateCode();
        } while (familyMapper.selectCount(new LambdaQueryWrapper<Family>().eq(Family::getCode, code)) > 0);

        Family family = Family.builder()
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .avatar(request.getAvatar())
                .creatorId(userId)
                .build();
        familyMapper.insert(family);

        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && user.getGlobalRole().isAdmin();

        String creatorName = user != null && user.getName() != null && !user.getName().isEmpty() 
                ? user.getName() 
                : (user != null ? user.getNickname() : "创建者");

        FamilyMember creatorMember = null;
        if (!isAdmin) {
            creatorMember = FamilyMember.builder()
                    .familyId(family.getId())
                    .userId(userId)
                    .name(creatorName)
                    .gender(GenderEnum.MALE)
                    .avatar(user != null ? user.getAvatar() : null)
                    .isCreator(1)
                    .build();
            memberMapper.insert(creatorMember);
        }

        userRoleService.createUserGenealogy(userId, family.getId(), "ADMIN", 
                creatorMember != null ? creatorMember.getId() : null, userId);

        return convertToDetailDTO(family);
    }

    @Override
    @Transactional
    public void joinFamily(FamilyJoinDTO request, Long userId) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new BusinessException("家谱码不能为空");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }

        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getCode, request.getCode())
        );
        if (family == null) {
            throw new BusinessException("家谱码无效");
        }

        LambdaQueryWrapper<JoinRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JoinRequest::getFamilyId, family.getId())
                .eq(JoinRequest::getApplicantUserId, userId)
                .eq(JoinRequest::getStatus, RequestStatusEnum.PENDING);
        if (joinRequestMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已提交过申请");
        }

        JoinRequest joinRequest = JoinRequest.builder()
                .familyId(family.getId())
                .applicantUserId(userId)
                .applicantName(request.getName())
                .relationDesc(request.getRelationDesc())
                .status(RequestStatusEnum.PENDING)
                .build();
        joinRequestMapper.insert(joinRequest);
    }

    @Override
    public FamilyDetailDTO getFamilyById(Long id) {
        Family family = familyMapper.selectById(id);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }
        return convertToDetailDTO(family);
    }

    @Override
    public List<FamilyDetailDTO> getMyFamilies(Long userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        List<FamilyMember> members = memberMapper.selectList(wrapper);

        List<Long> familyIds = members.stream()
                .map(FamilyMember::getFamilyId)
                .distinct()
                .toList();

        if (familyIds.isEmpty()) {
            return List.of();
        }

        List<Family> families = familyMapper.selectBatchIds(familyIds);
        return families.stream()
                .map(this::convertToDetailDTO)
                .toList();
    }

    @Override
    public FamilyDetailDTO getFamilyByCode(String code) {
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getCode, code)
        );
        if (family == null) {
            throw new BusinessException("家谱码无效");
        }
        return convertToDetailDTO(family);
    }

    @Override
    public List<FamilyDetailDTO> getAllFamilies() {
        List<Family> families = familyMapper.selectList(null);
        return families.stream()
                .map(this::convertToDetailDTO)
                .toList();
    }

    @Override
    public FamilyDetailDTO updateFamily(Long id, FamilyCreateDTO request, Long userId) {
        Family family = familyMapper.selectById(id);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            family.setName(request.getName());
        }
        family.setDescription(request.getDescription());
        family.setAvatar(request.getAvatar());

        familyMapper.updateById(family);
        return convertToDetailDTO(family);
    }

    @Override
    public void deleteFamily(Long id, Long userId) {
        Family family = familyMapper.selectById(id);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (!family.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }

        familyMapper.deleteById(id);
    }

    @Override
    public PageResult<FamilyDetailDTO> getFamiliesPaged(Integer page, Integer size) {
        Page<Family> pageParam = new Page<>(page, size);
        IPage<Family> familyPage = familyMapper.selectPage(pageParam, null);

        List<FamilyDetailDTO> records = familyPage.getRecords().stream()
                .map(this::convertToDetailDTO)
                .toList();

        return PageResult.of(records, familyPage.getTotal(), page, size);
    }

    private FamilyDetailDTO convertToDetailDTO(Family family) {
        FamilyDetailDTO dto = new FamilyDetailDTO();
        dto.setId(family.getId());
        dto.setName(family.getName());
        dto.setCode(family.getCode());
        dto.setAvatar(family.getAvatar());
        dto.setDescription(family.getDescription());
        dto.setCreatorId(family.getCreatorId());
        dto.setCreateTime(family.getCreateTime());

        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, family.getId())
        );
        dto.setMemberCount(count.intValue());

        return dto;
    }
}
