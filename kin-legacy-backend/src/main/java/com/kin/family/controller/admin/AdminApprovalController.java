package com.kin.family.controller.admin;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.dto.*;
import com.kin.family.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员审批控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/approvals")
    @RequireLogin
    public Result<PageResult<ApprovalDetailDTO>> getAllApprovals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(approvalService.getAllApprovals(type, status, page, size));
    }

    @PostMapping("/approval/{familyId}/{requestId}/handle")
    @RequireLogin
    @RequireRole("SUPER_ADMIN")
    public Result<Void> handleApproval(
            @PathVariable Long familyId,
            @PathVariable Long requestId,
            @RequestBody ApprovalHandleDTO request) {
        approvalService.handleApprovalAdmin(familyId, requestId, request);
        return Result.success();
    }
}
