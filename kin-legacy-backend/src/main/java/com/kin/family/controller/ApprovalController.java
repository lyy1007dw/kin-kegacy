package com.kin.family.controller;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.dto.*;
import com.kin.family.service.ApprovalService;
import com.kin.family.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family/{familyId}")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/approvals")
    @RequireLogin
    public Result<PageResult<ApprovalResponse>> getApprovals(
            @PathVariable Long familyId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(approvalService.getApprovals(familyId, type, status, page, size));
    }

    @PostMapping("/approval/{requestId}/handle")
    @RequireLogin
    public Result<Void> handleApproval(
            @PathVariable Long familyId,
            @PathVariable Long requestId,
            @RequestBody HandleApprovalRequest request) {
        Long userId = UserContext.getUserId();
        approvalService.handleApproval(familyId, requestId, request, userId);
        return Result.success();
    }
}
