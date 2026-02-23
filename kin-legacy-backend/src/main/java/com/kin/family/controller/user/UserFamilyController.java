package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
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
    public Result<FamilyDetailDTO> createFamily(@RequestBody FamilyCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.createFamily(request, userId));
    }

    @PostMapping("/join")
    @RequireLogin
    public Result<Void> joinFamily(@RequestBody FamilyJoinDTO request) {
        Long userId = UserContextUtil.getUserId();
        familyService.joinFamily(request, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<FamilyDetailDTO> getFamilyById(@PathVariable Long id) {
        return Result.success(familyService.getFamilyById(id));
    }

    @GetMapping("/mine")
    @RequireLogin
    public Result<List<FamilyDetailDTO>> getMyFamilies() {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.getMyFamilies(userId));
    }

    @GetMapping("/code/{code}")
    public Result<FamilyDetailDTO> getFamilyByCode(@PathVariable String code) {
        return Result.success(familyService.getFamilyByCode(code));
    }

    @GetMapping("/list")
    public Result<List<FamilyDetailDTO>> getAllFamilies() {
        return Result.success(familyService.getAllFamilies());
    }

    @GetMapping("/list/paged")
    public Result<PageResult<FamilyDetailDTO>> getFamiliesPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(familyService.getFamiliesPaged(page, size));
    }

    @PutMapping("/{id}")
    @RequireLogin
    public Result<FamilyDetailDTO> updateFamily(@PathVariable Long id, @RequestBody FamilyCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(familyService.updateFamily(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> deleteFamily(@PathVariable Long id) {
        Long userId = UserContextUtil.getUserId();
        familyService.deleteFamily(id, userId);
        return Result.success();
    }
}
