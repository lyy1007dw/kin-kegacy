package com.kin.family.controller.auth;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import com.kin.family.util.context.UserContext;
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

    @PostMapping("/wx-login")
    public Result<AuthTokenDTO> wxLogin(@RequestBody AuthWxLoginDTO request) {
        return Result.success(userService.wxLogin(request));
    }

    @PostMapping("/login")
    public Result<AuthTokenDTO> login(@RequestBody AuthLoginDTO request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
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
        UserContext.clear();
        return Result.success();
    }
}
