package com.kin.family.controller;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import com.kin.family.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/wx-login")
    public Result<LoginResponse> wxLogin(@RequestBody WxLoginRequest request) {
        return Result.success(userService.wxLogin(request));
    }

    @PostMapping("/login")
    @RequireRole("admin")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return Result.success(userService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/me")
    @RequireLogin
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    @RequireLogin
    public Result<Void> logout() {
        UserContext.clear();
        return Result.success();
    }
}
