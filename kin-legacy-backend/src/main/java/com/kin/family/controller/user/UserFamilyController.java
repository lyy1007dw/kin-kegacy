package com.kin.family.controller.user;

import com.kin.family.annotation.RequireAdmin;
import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.*;
import com.kin.family.service.FamilyService;
import com.kin.family.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户家谱控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class UserFamilyController {

    private final FamilyService familyService;

    @PostMapping("/create")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "创建家谱")
    public Result<FamilyDetailDTO> createFamily(@RequestBody FamilyCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.createFamily(request, userId));
    }

    @PostMapping("/join")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "加入家谱")
    public Result<Void> joinFamily(@RequestBody FamilyJoinDTO request) {
        Long userId = UserContextUtil.getUserId();
        familyService.joinFamily(request, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "查看家谱详情")
    public Result<FamilyDetailDTO> getFamilyById(@PathVariable Long id) {
        return Result.success(familyService.getFamilyById(id));
    }

    @GetMapping("/mine")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "查询我的家谱")
    public Result<List<FamilyDetailDTO>> getMyFamilies() {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.getMyFamilies(userId));
    }

    @GetMapping("/code/{code}")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "通过家谱码查询")
    public Result<FamilyDetailDTO> getFamilyByCode(@PathVariable String code) {
        return Result.success(familyService.getFamilyByCode(code));
    }

    @GetMapping("/list")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "查询所有家谱")
    public Result<List<FamilyDetailDTO>> getAllFamilies() {
        return Result.success(familyService.getAllFamilies());
    }

    @GetMapping("/list/paged")
    @RequireLogin
    @OperationLogger(module = "家谱管理", operation = "分页查询家谱")
    public Result<PageResult<FamilyDetailDTO>> getFamiliesPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(familyService.getFamiliesPaged(page, size));
    }

    @PutMapping("/{id}")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "家谱管理", operation = "更新家谱")
    public Result<FamilyDetailDTO> updateFamily(@PathVariable Long id, @RequestBody FamilyCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.updateFamily(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "家谱管理", operation = "删除家谱")
    public Result<Void> deleteFamily(@PathVariable Long id) {
        Long userId = UserContextUtil.getUserId();
        familyService.deleteFamily(id, userId);
        return Result.success();
    }
}
