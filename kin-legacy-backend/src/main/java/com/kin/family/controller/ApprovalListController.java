package com.kin.family.controller;

import com.kin.family.dto.*;
import com.kin.family.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalListController {

    private final ApprovalService approvalService;

    @GetMapping("/all")
    public Result<PageResult<ApprovalResponse>> getAllApprovals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(approvalService.getAllApprovals(type, status, page, size));
    }
}
