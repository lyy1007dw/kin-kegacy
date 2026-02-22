package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.config.jwt.JwtProperties;
import com.kin.family.config.WeChatConfig;
import com.kin.family.dto.LoginResponse;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.UserInfoResponse;
import com.kin.family.dto.WxLoginRequest;
import com.kin.family.entity.User;
import com.kin.family.enums.UserRole;
import com.kin.family.enums.UserStatus;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.UserService;
import com.kin.family.util.jwt.JwtUtil;
import com.kin.family.util.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public LoginResponse wxLogin(WxLoginRequest request) {
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
                    .role(UserRole.user)
                    .status(UserStatus.normal)
                    .build();
            userMapper.insert(user);
        }

        if (user.getStatus() == UserStatus.disabled) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }

        return buildLoginResponse(user);
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
    public LoginResponse login(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !password.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == UserStatus.disabled) {
            throw new BusinessException("该账号已被禁用，请联系管理员");
        }

        return buildLoginResponse(user);
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return getUserById(userId);
    }

    @Override
    public UserInfoResponse getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserInfo(user);
    }

    @Override
    public List<UserInfoResponse> getAllUsers() {
        List<User> users = userMapper.selectList(null);
        return users.stream()
                .map(this::convertToUserInfo)
                .toList();
    }

    @Override
    public List<UserInfoResponse> getNonAdminUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(User::getRole, UserRole.admin);
        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(this::convertToUserInfo)
                .toList();
    }

    @Override
    public PageResult<UserInfoResponse> getUsersPaged(Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        IPage<User> userPage = userMapper.selectPage(pageParam, null);
        
        List<UserInfoResponse> records = userPage.getRecords().stream()
                .map(this::convertToUserInfo)
                .toList();
        
        return PageResult.of(records, userPage.getTotal(), page, size);
    }

    @Override
    public UserInfoResponse updateUser(Long userId, UserInfoResponse request) {
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
        return convertToUserInfo(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getRole() == UserRole.admin) {
            throw new BusinessException("不能禁用管理员用户");
        }

        user.setStatus(UserStatus.disabled);
        userMapper.updateById(user);
    }

    @Override
    public void enableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(UserStatus.normal);
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);
        
        // 家谱总数
        long familyCount = familyMapper.selectCount(null);
        stats.put("familyCount", familyCount);
        
        // 成员总数
        long memberCount = memberMapper.selectCount(null);
        stats.put("memberCount", memberCount);
        
        // 待审批数
        long pendingCount = joinRequestMapper.selectCount(
                new LambdaQueryWrapper<com.kin.family.entity.JoinRequest>()
                        .eq(com.kin.family.entity.JoinRequest::getStatus, 
                            com.kin.family.enums.RequestStatus.pending)
        );
        stats.put("pendingApproval", pendingCount);
        
        return stats;
    }

    private UserInfoResponse convertToUserInfo(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        return response;
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtProperties.getExpiration() / 1000);
        response.setUserInfo(convertToUserInfo(user));

        UserContext.setUserId(user.getId());
        return response;
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "Refresh token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == UserStatus.disabled) {
            throw new BusinessException("该账号已被禁用");
        }

        return buildLoginResponse(user);
    }
}
