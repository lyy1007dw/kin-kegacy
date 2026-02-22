package com.kin.family.controller.admin;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminFamilyController {

    private final FamilyService familyService;

    @GetMapping("/family/list")
    @RequireLogin
    @RequireRole("admin")
    public Result<List<FamilyDetailResponse>> getAllFamilies() {
        return Result.success(familyService.getAllFamilies());
    }

    @GetMapping("/family/list/paged")
    @RequireLogin
    @RequireRole("admin")
    public Result<PageResult<FamilyDetailResponse>> getFamiliesPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(familyService.getFamiliesPaged(page, size));
    }
}
