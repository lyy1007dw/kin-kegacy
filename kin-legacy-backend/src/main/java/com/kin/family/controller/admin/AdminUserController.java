package com.kin.family.controller.admin;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/statistics")
    @RequireLogin
    @RequireRole("admin")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(userService.getStatistics());
    }

    @GetMapping("/user/list")
    @RequireLogin
    @RequireRole("admin")
    public Result<List<UserInfoResponse>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/user/list/non-admin")
    @RequireLogin
    @RequireRole("admin")
    public Result<List<UserInfoResponse>> getNonAdminUsers() {
        return Result.success(userService.getNonAdminUsers());
    }

    @GetMapping("/user/list/paged")
    @RequireLogin
    @RequireRole("admin")
    public Result<PageResult<UserInfoResponse>> getUsersPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.getUsersPaged(page, size));
    }

    @PutMapping("/user/{id}")
    @RequireLogin
    @RequireRole("admin")
    public Result<UserInfoResponse> updateUser(@PathVariable Long id, @RequestBody UserInfoResponse request) {
        return Result.success(userService.updateUser(id, request));
    }

    @PutMapping("/user/{id}/disable")
    @RequireLogin
    @RequireRole("admin")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }

    @PutMapping("/user/{id}/enable")
    @RequireLogin
    @RequireRole("admin")
    public Result<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success();
    }
}
