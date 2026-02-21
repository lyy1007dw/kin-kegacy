package com.kin.family.service;

import com.kin.family.dto.ApprovalResponse;
import com.kin.family.dto.HandleApprovalRequest;

import com.kin.family.dto.PageResult;

import java.util.List;

public interface ApprovalService {
    PageResult<ApprovalResponse> getApprovals(Long familyId, String type, String status, Integer page, Integer size);
    PageResult<ApprovalResponse> getAllApprovals(String type, String status, Integer page, Integer size);
    void handleApproval(Long familyId, Long requestId, HandleApprovalRequest request, Long userId);
    void handleApprovalAdmin(Long familyId, Long requestId, HandleApprovalRequest request);
}
