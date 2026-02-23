package com.kin.family.service;

import com.kin.family.dto.UserGenealogyDTO;
import com.kin.family.entity.UserGenealogy;

import java.util.List;

/**
 * 用户角色管理服务
 *
 * @author candong
 */
public interface UserRoleService {

    /**
     * 更新用户在家谱中的角色
     *
     * @param userId      用户ID
     * @param genealogyId 家谱ID
     * @param role        角色
     */
    void updateUserRole(Long userId, Long genealogyId, String role);

    /**
     * 检查并升级用户全局角色
     *
     * @param userId 用户ID
     */
    void checkAndUpgradeGlobalRole(Long userId);

    /**
     * 检查并降级用户全局角色
     *
     * @param userId 用户ID
     */
    void checkAndDowngradeGlobalRole(Long userId);

    /**
     * 判断用户是否为某家谱的管理员
     *
     * @param userId      用户ID
     * @param genealogyId 家谱ID
     * @return 是否为管理员
     */
    boolean isAdminOfGenealogy(Long userId, Long genealogyId);

    /**
     * 判断用户是否为某家谱的唯一管理员
     *
     * @param userId      用户ID
     * @param genealogyId 家谱ID
     * @return 是否为唯一管理员
     */
    boolean isOnlyAdminOfGenealogy(Long userId, Long genealogyId);

    /**
     * 获取家谱管理员数量
     *
     * @param genealogyId 家谱ID
     * @return 管理员数量
     */
    int getAdminCount(Long genealogyId);

    /**
     * 获取用户所属家谱列表
     *
     * @param userId 用户ID
     * @return 家谱列表
     */
    List<UserGenealogyDTO> getUserGenealogies(Long userId);

    /**
     * 获取用户在家谱中的关联记录
     *
     * @param userId      用户ID
     * @param genealogyId 家谱ID
     * @return 关联记录
     */
    UserGenealogy getUserGenealogy(Long userId, Long genealogyId);

    /**
     * 创建用户-家谱关联
     *
     * @param userId         用户ID
     * @param genealogyId    家谱ID
     * @param role           角色
     * @param familyMemberId 关联成员ID
     * @param createdBy      创建人ID
     */
    void createUserGenealogy(Long userId, Long genealogyId, String role, Long familyMemberId, Long createdBy);
}
