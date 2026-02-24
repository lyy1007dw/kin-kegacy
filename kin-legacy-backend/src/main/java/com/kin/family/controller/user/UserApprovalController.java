package com.kin.family.controller.user;

import com.kin.family.annotation.RequireAdmin;
import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.constant.UserRoleEnum;
import com.kin.family.dto.*;
import com.kin.family.service.ApprovalService;
import com.kin.family.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户审批控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class UserApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    @RequireLogin
    public Result<PageResult<ApprovalDetailDTO>> getApprovals(
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

    @GetMapping("/family/{familyId}")
    @RequireLogin
    @RequireAdmin
    public Result<PageResult<ApprovalDetailDTO>> getFamilyApprovals(
            @PathVariable Long familyId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(approvalService.getApprovals(familyId, type, status, page, size));
    }

    @PostMapping("/{requestId}/handle")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "审批管理", operation = "处理审批")
    public Result<Void> handleApproval(
            @PathVariable Long requestId,
            @RequestParam Long familyId,
            @RequestBody ApprovalHandleDTO request) {
        Long userId = UserContextUtil.getUserId();
        approvalService.handleApproval(familyId, requestId, request, userId);
        return Result.success();
    }
}
