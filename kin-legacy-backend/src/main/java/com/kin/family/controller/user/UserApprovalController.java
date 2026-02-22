package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.dto.*;
import com.kin.family.service.ApprovalService;
import com.kin.family.util.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class UserApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    @RequireLogin
    public Result<PageResult<ApprovalResponse>> getApprovals(
            @RequestParam(required = false) Long familyId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (familyId != null) {
            return Result.success(approvalService.getApprovals(familyId, type, status, page, size));
        } else {
            return Result.success(approvalService.getAllApprovals(type, status, page, size));
        }
    }

    @PostMapping("/{requestId}/handle")
    @RequireLogin
    public Result<Void> handleApproval(
            @PathVariable Long requestId,
            @RequestParam Long familyId,
            @RequestBody HandleApprovalRequest request) {
        Long userId = UserContext.getUserId();
        approvalService.handleApproval(familyId, requestId, request, userId);
        return Result.success();
    }
}
