package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kin.family.constant.GenealogyRoleEnum;
import com.kin.family.constant.UserRoleEnum;
import com.kin.family.dto.UserGenealogyDTO;
import com.kin.family.entity.Family;
import com.kin.family.entity.User;
import com.kin.family.entity.UserGenealogy;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.UserGenealogyMapper;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户角色管理服务实现
 *
 * @author candong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserGenealogyMapper userGenealogyMapper;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(Long userId, Long genealogyId, String role) {
        GenealogyRoleEnum roleEnum = GenealogyRoleEnum.valueOf(role);

        if (roleEnum == GenealogyRoleEnum.MEMBER && isOnlyAdminOfGenealogy(userId, genealogyId)) {
            throw new BusinessException("该用户是该家谱的唯一管理员，无法降级");
        }

        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        if (ug != null) {
            ug.setRole(roleEnum);
            userGenealogyMapper.updateById(ug);
        } else {
            createUserGenealogy(userId, genealogyId, role, null, null);
        }

        if (roleEnum == GenealogyRoleEnum.ADMIN) {
            checkAndUpgradeGlobalRole(userId);
        } else {
            checkAndDowngradeGlobalRole(userId);
        }

        log.info("更新用户角色: userId={}, genealogyId={}, role={}", userId, genealogyId, role);
    }

    @Override
    public void checkAndUpgradeGlobalRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        if (user.getGlobalRole() == UserRoleEnum.NORMAL_USER) {
            Long adminCount = userGenealogyMapper.selectCount(
                    new LambdaQueryWrapper<UserGenealogy>()
                            .eq(UserGenealogy::getUserId, userId)
                            .eq(UserGenealogy::getRole, GenealogyRoleEnum.ADMIN)
            );
            if (adminCount > 0) {
                user.setGlobalRole(UserRoleEnum.GENEALOGY_ADMIN);
                userMapper.updateById(user);
                log.info("用户全局角色升级: userId={}, role={}", userId, UserRoleEnum.GENEALOGY_ADMIN);
            }
        }
    }

    @Override
    public void checkAndDowngradeGlobalRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        if (user.getGlobalRole() == UserRoleEnum.GENEALOGY_ADMIN) {
            Long adminCount = userGenealogyMapper.selectCount(
                    new LambdaQueryWrapper<UserGenealogy>()
                            .eq(UserGenealogy::getUserId, userId)
                            .eq(UserGenealogy::getRole, GenealogyRoleEnum.ADMIN)
            );
            if (adminCount == 0) {
                user.setGlobalRole(UserRoleEnum.NORMAL_USER);
                userMapper.updateById(user);
                log.info("用户全局角色降级: userId={}, role={}", userId, UserRoleEnum.NORMAL_USER);
            }
        }
    }

    @Override
    public boolean isAdminOfGenealogy(Long userId, Long genealogyId) {
        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        return ug != null && ug.getRole() == GenealogyRoleEnum.ADMIN;
    }

    @Override
    public boolean isOnlyAdminOfGenealogy(Long userId, Long genealogyId) {
        int adminCount = userGenealogyMapper.countAdmins(genealogyId);
        if (adminCount != 1) {
            return false;
        }

        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        return ug != null && ug.getRole() == GenealogyRoleEnum.ADMIN;
    }

    @Override
    public int getAdminCount(Long genealogyId) {
        return userGenealogyMapper.countAdmins(genealogyId);
    }

    @Override
    public List<UserGenealogyDTO> getUserGenealogies(Long userId) {
        List<UserGenealogy> relations = userGenealogyMapper.selectList(
                new LambdaQueryWrapper<UserGenealogy>()
                        .eq(UserGenealogy::getUserId, userId)
        );

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> genealogyIds = relations.stream()
                .map(UserGenealogy::getGenealogyId)
                .toList();

        List<Family> families = familyMapper.selectBatchIds(genealogyIds);
        Map<Long, String> familyNameMap = families.stream()
                .collect(Collectors.toMap(Family::getId, Family::getName));

        return relations.stream()
                .map(ug -> {
                    UserGenealogyDTO dto = new UserGenealogyDTO();
                    dto.setGenealogyId(ug.getGenealogyId());
                    dto.setGenealogyName(familyNameMap.get(ug.getGenealogyId()));
                    dto.setRole(ug.getRole().getValue());
                    dto.setJoinedAt(ug.getJoinedAt());
                    return dto;
                })
                .toList();
    }

    @Override
    public UserGenealogy getUserGenealogy(Long userId, Long genealogyId) {
        return userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserGenealogy(Long userId, Long genealogyId, String role, Long familyMemberId, Long createdBy) {
        GenealogyRoleEnum roleEnum = GenealogyRoleEnum.valueOf(role);

        UserGenealogy existing = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        if (existing != null) {
            existing.setRole(roleEnum);
            if (familyMemberId != null) {
                existing.setFamilyMemberId(familyMemberId);
            }
            userGenealogyMapper.updateById(existing);
            return;
        }

        UserGenealogy ug = UserGenealogy.builder()
                .userId(userId)
                .genealogyId(genealogyId)
                .role(roleEnum)
                .familyMemberId(familyMemberId)
                .joinedAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
        userGenealogyMapper.insert(ug);

        if (roleEnum == GenealogyRoleEnum.ADMIN) {
            checkAndUpgradeGlobalRole(userId);
        }

        log.info("创建用户-家谱关联: userId={}, genealogyId={}, role={}", userId, genealogyId, role);
    }
}
