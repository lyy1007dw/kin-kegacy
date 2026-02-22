package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.dto.*;
import com.kin.family.service.FamilyService;
import com.kin.family.util.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class UserFamilyController {

    private final FamilyService familyService;

    @PostMapping("/create")
    @RequireLogin
    public Result<FamilyDetailResponse> createFamily(@RequestBody CreateFamilyRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(familyService.createFamily(request, userId));
    }

    @PostMapping("/join")
    @RequireLogin
    public Result<Void> joinFamily(@RequestBody JoinFamilyRequest request) {
        Long userId = UserContext.getUserId();
        familyService.joinFamily(request, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<FamilyDetailResponse> getFamilyById(@PathVariable Long id) {
        return Result.success(familyService.getFamilyById(id));
    }

    @GetMapping("/mine")
    @RequireLogin
    public Result<List<FamilyDetailResponse>> getMyFamilies() {
        Long userId = UserContext.getUserId();
        return Result.success(familyService.getMyFamilies(userId));
    }

    @GetMapping("/code/{code}")
    public Result<FamilyDetailResponse> getFamilyByCode(@PathVariable String code) {
        return Result.success(familyService.getFamilyByCode(code));
    }

    @GetMapping("/list")
    public Result<List<FamilyDetailResponse>> getAllFamilies() {
        return Result.success(familyService.getAllFamilies());
    }

    @GetMapping("/list/paged")
    public Result<PageResult<FamilyDetailResponse>> getFamiliesPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(familyService.getFamiliesPaged(page, size));
    }

    @PutMapping("/{id}")
    @RequireLogin
    public Result<FamilyDetailResponse> updateFamily(@PathVariable Long id, @RequestBody CreateFamilyRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(familyService.updateFamily(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> deleteFamily(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        familyService.deleteFamily(id, userId);
        return Result.success();
    }
}
