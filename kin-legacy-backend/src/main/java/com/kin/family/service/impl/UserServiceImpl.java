package com.kin.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.LoginResponse;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.UserInfoResponse;
import com.kin.family.dto.WxLoginRequest;
import com.kin.family.entity.User;
import com.kin.family.enums.UserRole;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.UserMapper;
import com.kin.family.service.UserService;
import com.kin.family.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final com.kin.family.mapper.FamilyMapper familyMapper;
    private final com.kin.family.mapper.FamilyMemberMapper memberMapper;
    private final com.kin.family.mapper.JoinRequestMapper joinRequestMapper;

    @Override
    public LoginResponse wxLogin(WxLoginRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new BusinessException("code不能为空");
        }

        String openid = "wx_" + request.getCode();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .nickname("用户" + request.getCode().substring(0, 4))
                    .role(UserRole.user)
                    .build();
            userMapper.insert(user);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(convertToUserInfo(user));
        
        UserContext.setUserId(user.getId());
        
        return response;
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

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 简单密码验证（生产环境应加密存储和验证）
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(convertToUserInfo(user));
        
        UserContext.setUserId(user.getId());
        
        return response;
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

        if (user.getId() == 1) {
            throw new BusinessException("不能禁用管理员用户");
        }

        userMapper.deleteById(userId);
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
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}
