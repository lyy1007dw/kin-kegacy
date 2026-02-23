package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.config.jwt.JwtProperties;
import com.kin.family.config.WeChatConfig;
import com.kin.family.dto.AuthTokenDTO;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.UserDetailDTO;
import com.kin.family.dto.AuthWxLoginDTO;
import com.kin.family.entity.User;
import com.kin.family.constant.UserRoleEnum;
import com.kin.family.constant.UserStatusEnum;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.UserService;
import com.kin.family.util.PasswordUtil;
import com.kin.family.util.JwtUtil;
import com.kin.family.util.UserContextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 *
 * @author candong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final com.kin.family.mapper.FamilyMapper familyMapper;
    private final com.kin.family.mapper.FamilyMemberMapper memberMapper;
    private final com.kin.family.mapper.JoinRequestMapper joinRequestMapper;
    private final WeChatConfig weChatConfig;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public AuthTokenDTO wxLogin(AuthWxLoginDTO request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new BusinessException("code不能为空");
        }

        String openid = getOpenidFromWechat(request.getCode());
        if (openid == null) {
            throw new BusinessException("微信登录失败，请重试");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .nickname("用户" + request.getCode().substring(0, 4))
                    .globalRole(UserRoleEnum.NORMAL_USER)
                    .status(UserStatusEnum.NORMAL)
                    .loginFailCount(0)
                    .build();
            userMapper.insert(user);
        }

        if (user.getStatus() == UserStatusEnum.DISABLED) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }

        return buildAuthTokenDTO(user);
    }

    private String getOpenidFromWechat(String code) {
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                weChatConfig.getAppid(),
                weChatConfig.getSecret(),
                code
            );
            RestTemplate restTemplate = new RestTemplate();
            String responseStr = restTemplate.getForObject(url, String.class);

            log.info("微信登录响应: {}", responseStr);

            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);

            if (response != null) {
                if (response.get("openid") != null) {
                    return (String) response.get("openid");
                }
                if (response.get("errcode") != null) {
                    Integer errcode = (Integer) response.get("errcode");
                    String errmsg = (String) response.get("errmsg");
                    throw new BusinessException("微信登录失败: " + errmsg + " (code: " + errcode + ")");
                }
            }
            throw new BusinessException("微信登录失败: 未获取到openid");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new BusinessException("微信登录失败: " + e.getMessage());
        }
    }

    @Override
    public AuthTokenDTO login(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == UserStatusEnum.DISABLED) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }

        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("账号已被锁定，请稍后再试");
        }

        String storedPassword = user.getPassword();
        boolean passwordMatches;
        if (PasswordUtil.isEncoded(storedPassword)) {
            passwordMatches = PasswordUtil.matches(password, storedPassword);
        } else {
            passwordMatches = password.equals(storedPassword);
        }

        if (!passwordMatches) {
            int failCount = user.getLoginFailCount() != null ? user.getLoginFailCount() + 1 : 1;
            user.setLoginFailCount(failCount);
            if (failCount >= 5) {
                user.setLockTime(LocalDateTime.now().plusMinutes(30));
            }
            userMapper.updateById(user);
            throw new BusinessException("用户名或密码错误");
        }

        user.setLoginFailCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return buildAuthTokenDTO(user);
    }

    @Override
    public UserDetailDTO getCurrentUser() {
        Long userId = UserContextUtil.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return getUserById(userId);
    }

    @Override
    public UserDetailDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserDetailDTO(user);
    }

    @Override
    public List<UserDetailDTO> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToUserDetailDTO)
                .toList();
    }

    @Override
    public List<UserDetailDTO> getNonAdminUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(User::getGlobalRole, UserRoleEnum.SUPER_ADMIN);
        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(this::convertToUserDetailDTO)
                .toList();
    }

    @Override
    public PageResult<UserDetailDTO> getUsersPaged(Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> userPage = userMapper.selectPage(pageParam, null);

        List<UserDetailDTO> records = userPage.getRecords().stream()
                .map(this::convertToUserDetailDTO)
                .toList();

        return PageResult.of(records, userPage.getTotal(), page, size);
    }

    @Override
    public UserDetailDTO updateUser(Long userId, UserDetailDTO request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);
        return convertToUserDetailDTO(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getGlobalRole() == UserRoleEnum.SUPER_ADMIN) {
            throw new BusinessException("不能禁用超级管理员");
        }

        user.setStatus(UserStatusEnum.DISABLED);
        userMapper.updateById(user);
    }

    @Override
    public void enableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(UserStatusEnum.NORMAL);
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);

        long familyCount = familyMapper.selectCount(null);
        stats.put("familyCount", familyCount);

        long memberCount = memberMapper.selectCount(null);
        stats.put("memberCount", memberCount);

        long pendingCount = joinRequestMapper.selectCount(
                new LambdaQueryWrapper<com.kin.family.entity.JoinRequest>()
                        .eq(com.kin.family.entity.JoinRequest::getStatus,
                            com.kin.family.constant.RequestStatusEnum.PENDING)
        );
        stats.put("pendingApproval", pendingCount);

        return stats;
    }

    private UserDetailDTO convertToUserDetailDTO(User user) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setName(user.getName());
        dto.setNameRequired(user.getName() == null || user.getName().isEmpty());
        dto.setGlobalRole(user.getGlobalRole());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }

    private AuthTokenDTO buildAuthTokenDTO(User user) {
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getGlobalRole().getValue());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        AuthTokenDTO dto = new AuthTokenDTO();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setTokenType("Bearer");
        dto.setExpiresIn(jwtProperties.getExpiration() / 1000);
        dto.setUserInfo(convertToUserDetailDTO(user));

        UserContextUtil.setUserId(user.getId());
        return dto;
    }

    public AuthTokenDTO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "Refresh token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == UserStatusEnum.DISABLED) {
            throw new BusinessException("该账号已被禁用");
        }

        return buildAuthTokenDTO(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateUserName(Long userId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldName = user.getName();
        user.setName(newName.trim());
        userMapper.updateById(user);

        List<com.kin.family.entity.FamilyMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<com.kin.family.entity.FamilyMember>()
                        .eq(com.kin.family.entity.FamilyMember::getUserId, userId)
        );

        for (com.kin.family.entity.FamilyMember member : members) {
            member.setName(newName.trim());
            memberMapper.updateById(member);
        }

        log.info("用户姓名修改：userId={}, {} -> {}, 同步更新了 {} 个成员", userId, oldName, newName, members.size());
    }
}
