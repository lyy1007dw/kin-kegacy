package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/me")
    @RequireLogin
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/user/{id}")
    @RequireLogin
    public Result<UserInfoResponse> updateUser(@PathVariable Long id, @RequestBody UserInfoResponse request) {
        return Result.success(userService.updateUser(id, request));
    }
}
