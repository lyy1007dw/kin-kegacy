package com.kin.family.controller;

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
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        UserContext.clear();
        return Result.success();
    }
}
