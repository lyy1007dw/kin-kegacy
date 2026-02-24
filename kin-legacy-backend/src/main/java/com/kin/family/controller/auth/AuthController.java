package com.kin.family.controller.auth;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import com.kin.family.service.LoginLogService;
import com.kin.family.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginLogService loginLogService;

    @PostMapping("/wx-login")
    @OperationLogger(module = "认证", operation = "微信登录")
    public Result<AuthTokenDTO> wxLogin(@RequestBody AuthWxLoginDTO request, HttpServletRequest httpRequest) {
        try {
            AuthTokenDTO tokenDTO = userService.wxLogin(request);
            loginLogService.recordLoginLog(
                    tokenDTO.getUserInfo().getId(),
                    tokenDTO.getUserInfo().getNickname(),
                    "WECHAT",
                    true,
                    null,
                    httpRequest
            );
            return Result.success(tokenDTO);
        } catch (Exception e) {
            loginLogService.recordLoginLog(
                    null,
                    request.getCode() != null ? request.getCode().substring(0, Math.min(4, request.getCode().length())) : "unknown",
                    "WECHAT",
                    false,
                    e.getMessage(),
                    httpRequest
            );
            throw e;
        }
    }

    @PostMapping("/login")
    @OperationLogger(module = "认证", operation = "账号密码登录")
    public Result<AuthTokenDTO> login(@RequestBody AuthLoginDTO request, HttpServletRequest httpRequest) {
        try {
            AuthTokenDTO tokenDTO = userService.login(request.getUsername(), request.getPassword());
            loginLogService.recordLoginLog(
                    tokenDTO.getUserInfo().getId(),
                    request.getUsername(),
                    "PASSWORD",
                    true,
                    null,
                    httpRequest
            );
            return Result.success(tokenDTO);
        } catch (Exception e) {
            loginLogService.recordLoginLog(
                    null,
                    request.getUsername(),
                    "PASSWORD",
                    false,
                    e.getMessage(),
                    httpRequest
            );
            throw e;
        }
    }

    @PostMapping("/refresh")
    public Result<AuthTokenDTO> refresh(@RequestBody AuthRefreshTokenDTO request) {
        return Result.success(userService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/me")
    @RequireLogin
    public Result<UserDetailDTO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    @RequireLogin
    public Result<Void> logout() {
        UserContextUtil.clear();
        return Result.success();
    }
}
