package com.kin.family.service;

import com.kin.family.dto.LoginResponse;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.UserInfoResponse;
import com.kin.family.dto.WxLoginRequest;
import com.kin.family.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    LoginResponse wxLogin(WxLoginRequest request);
    LoginResponse login(String username, String password);
    UserInfoResponse getCurrentUser();
    UserInfoResponse getUserById(Long userId);
    List<UserInfoResponse> getAllUsers();
    List<UserInfoResponse> getNonAdminUsers();
    PageResult<UserInfoResponse> getUsersPaged(Integer page, Integer size);
    UserInfoResponse updateUser(Long userId, UserInfoResponse request);
    void disableUser(Long userId);
    Map<String, Object> getStatistics();
}
