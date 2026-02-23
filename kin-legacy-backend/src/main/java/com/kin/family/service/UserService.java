package com.kin.family.service;

import com.kin.family.dto.AuthTokenDTO;
import com.kin.family.dto.AuthWxLoginDTO;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.UserDetailDTO;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author candong
 */
public interface UserService {
    AuthTokenDTO wxLogin(AuthWxLoginDTO request);
    AuthTokenDTO login(String username, String password);
    AuthTokenDTO refreshToken(String refreshToken);
    UserDetailDTO getCurrentUser();
    UserDetailDTO getUserById(Long userId);
    List<UserDetailDTO> getAllUsers();
    List<UserDetailDTO> getNonAdminUsers();
    PageResult<UserDetailDTO> getUsersPaged(Integer page, Integer size);
    UserDetailDTO updateUser(Long userId, UserDetailDTO request);
    void disableUser(Long userId);
    void enableUser(Long userId);
    Map<String, Object> getStatistics();
}
