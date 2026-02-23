package com.kin.family.service;

import com.kin.family.dto.ApprovalDetailDTO;
import com.kin.family.dto.ApprovalHandleDTO;
import com.kin.family.dto.PageResult;

/**
 * 审批服务接口
 *
 * @author candong
 */
public interface ApprovalService {
    PageResult<ApprovalDetailDTO> getApprovals(Long familyId, String type, String status, Integer page, Integer size);
    PageResult<ApprovalDetailDTO> getAllApprovals(String type, String status, Integer page, Integer size);
    void handleApproval(Long familyId, Long requestId, ApprovalHandleDTO request, Long userId);
    void handleApprovalAdmin(Long familyId, Long requestId, ApprovalHandleDTO request);
}
