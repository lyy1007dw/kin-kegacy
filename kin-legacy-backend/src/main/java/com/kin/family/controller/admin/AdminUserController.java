package com.kin.family.controller.admin;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户控制器
 *
 * @author candong
 */
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
    public Result<List<UserDetailDTO>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/user/list/non-admin")
    @RequireLogin
    @RequireRole("admin")
    public Result<List<UserDetailDTO>> getNonAdminUsers() {
        return Result.success(userService.getNonAdminUsers());
    }

    @GetMapping("/user/list/paged")
    @RequireLogin
    @RequireRole("admin")
    public Result<PageResult<UserDetailDTO>> getUsersPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.getUsersPaged(page, size));
    }

    @PutMapping("/user/{id}")
    @RequireLogin
    @RequireRole("admin")
    public Result<UserDetailDTO> updateUser(@PathVariable Long id, @RequestBody UserDetailDTO request) {
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
