package com.kin.family.controller;

import com.kin.family.dto.PageResult;
import com.kin.family.dto.Result;
import com.kin.family.dto.UserInfoResponse;
import com.kin.family.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(userService.getStatistics());
    }

    @GetMapping("/user/me")
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @GetMapping("/user/list")
    public Result<List<UserInfoResponse>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/user/list/non-admin")
    public Result<List<UserInfoResponse>> getNonAdminUsers() {
        return Result.success(userService.getNonAdminUsers());
    }

    @GetMapping("/user/list/paged")
    public Result<PageResult<UserInfoResponse>> getUsersPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.getUsersPaged(page, size));
    }

    @PutMapping("/user/{id}")
    public Result<UserInfoResponse> updateUser(@PathVariable Long id, @RequestBody UserInfoResponse request) {
        return Result.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }
}
