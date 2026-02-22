package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.CreateFamilyRequest;
import com.kin.family.dto.FamilyDetailResponse;
import com.kin.family.dto.JoinFamilyRequest;
import com.kin.family.dto.PageResult;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.JoinRequest;
import com.kin.family.entity.User;
import com.kin.family.enums.Gender;
import com.kin.family.enums.RequestStatus;
import com.kin.family.enums.UserRole;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.JoinRequestMapper;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.FamilyService;
import com.kin.family.util.code.FamilyCodeUtil;
import com.kin.family.util.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final JoinRequestMapper joinRequestMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public FamilyDetailResponse createFamily(CreateFamilyRequest request, Long userId) {
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

        // 检查用户是否是管理员，管理员不自动加入成员
        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && user.getRole() == UserRole.admin;
        
        // 只有非管理员用户才自动加入成员
        if (!isAdmin) {
            FamilyMember creator = FamilyMember.builder()
                    .familyId(family.getId())
                    .userId(userId)
                    .name(user != null ? user.getNickname() : "创建者")
                    .gender(Gender.male)
                    .avatar(user != null ? user.getAvatar() : null)
                    .isCreator(1)
                    .build();
            memberMapper.insert(creator);
        }

        return convertToDetailResponse(family);
    }

    @Override
    @Transactional
    public void joinFamily(JoinFamilyRequest request, Long userId) {
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
                .eq(JoinRequest::getStatus, RequestStatus.pending);
        if (joinRequestMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已提交过申请");
        }

        JoinRequest joinRequest = JoinRequest.builder()
                .familyId(family.getId())
                .applicantUserId(userId)
                .applicantName(request.getName())
                .relationDesc(request.getRelationDesc())
                .status(RequestStatus.pending)
                .build();
        joinRequestMapper.insert(joinRequest);
    }

    @Override
    public FamilyDetailResponse getFamilyById(Long id) {
        Family family = familyMapper.selectById(id);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }
        return convertToDetailResponse(family);
    }

    @Override
    public List<FamilyDetailResponse> getMyFamilies(Long userId) {
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
                .map(this::convertToDetailResponse)
                .toList();
    }

    @Override
    public FamilyDetailResponse getFamilyByCode(String code) {
        Family family = familyMapper.selectOne(
                new LambdaQueryWrapper<Family>().eq(Family::getCode, code)
        );
        if (family == null) {
            throw new BusinessException("家谱码无效");
        }
        return convertToDetailResponse(family);
    }

    @Override
    public List<FamilyDetailResponse> getAllFamilies() {
        List<Family> families = familyMapper.selectList(null);
        return families.stream()
                .map(this::convertToDetailResponse)
                .toList();
    }

    @Override
    public FamilyDetailResponse updateFamily(Long id, CreateFamilyRequest request, Long userId) {
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
        return convertToDetailResponse(family);
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
    public PageResult<FamilyDetailResponse> getFamiliesPaged(Integer page, Integer size) {
        Page<Family> pageParam = new Page<>(page, size);
        IPage<Family> familyPage = familyMapper.selectPage(pageParam, null);
        
        List<FamilyDetailResponse> records = familyPage.getRecords().stream()
                .map(this::convertToDetailResponse)
                .toList();
        
        return PageResult.of(records, familyPage.getTotal(), page, size);
    }

    private FamilyDetailResponse convertToDetailResponse(Family family) {
        FamilyDetailResponse response = new FamilyDetailResponse();
        response.setId(family.getId());
        response.setName(family.getName());
        response.setCode(family.getCode());
        response.setAvatar(family.getAvatar());
        response.setDescription(family.getDescription());
        response.setCreatorId(family.getCreatorId());
        response.setCreateTime(family.getCreateTime());
        
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, family.getId())
        );
        response.setMemberCount(count.intValue());
        
        return response;
    }
}
