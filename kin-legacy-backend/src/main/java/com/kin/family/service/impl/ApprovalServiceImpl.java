package com.kin.family.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.ApprovalResponse;
import com.kin.family.dto.HandleApprovalRequest;
import com.kin.family.dto.PageResult;
import com.kin.family.entity.EditRequest;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.JoinRequest;
import com.kin.family.enums.Gender;
import com.kin.family.enums.RequestStatus;
import com.kin.family.exception.BusinessException;
import com.kin.family.mapper.ApprovalMapper;
import com.kin.family.mapper.EditRequestMapper;
import com.kin.family.mapper.FamilyMapper;
import com.kin.family.mapper.FamilyMemberMapper;
import com.kin.family.mapper.JoinRequestMapper;
import com.kin.family.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final JoinRequestMapper joinRequestMapper;
    private final EditRequestMapper editRequestMapper;
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final ApprovalMapper approvalMapper;

    @Override
    public PageResult<ApprovalResponse> getApprovals(Long familyId, String type, String status, Integer page, Integer size) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        Page<ApprovalResponse> pageParam = new Page<>(page, size);
        List<ApprovalResponse> records = approvalMapper.getApprovalsByFamilyId(pageParam, familyId, type, status);
        
        return PageResult.of(records, pageParam.getTotal(), page, size);
    }

    @Override
    public PageResult<ApprovalResponse> getAllApprovals(String type, String status, Integer page, Integer size) {
        Page<ApprovalResponse> pageParam = new Page<>(page, size);
        List<ApprovalResponse> records = approvalMapper.getAllApprovals(pageParam, type, status);

        return PageResult.of(records, pageParam.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void handleApproval(Long familyId, Long requestId, HandleApprovalRequest request, Long userId) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        // 管理后台暂时跳过权限验证
        // if (!family.getCreatorId().equals(userId)) {
        //     throw new BusinessException(403, "只有创建者才能处理审批");
        // }

        if (request.getAction() == null || 
            (!request.getAction().equals("approve") && !request.getAction().equals("reject"))) {
            throw new BusinessException("action必须是approve或reject");
        }

        JoinRequest joinRequest = joinRequestMapper.selectById(requestId);
        if (joinRequest != null && joinRequest.getFamilyId().equals(familyId)) {
            handleJoinRequest(joinRequest, request, userId);
            return;
        }

        EditRequest editRequest = editRequestMapper.selectById(requestId);
        if (editRequest != null && editRequest.getFamilyId().equals(familyId)) {
            handleEditRequest(editRequest, request, userId);
            return;
        }

        throw new BusinessException("申请不存在");
    }

    private void handleJoinRequest(JoinRequest joinRequest, HandleApprovalRequest request, Long userId) {
        RequestStatus newStatus = "approve".equals(request.getAction()) ? 
                RequestStatus.approved : RequestStatus.rejected;
        
        joinRequest.setStatus(newStatus);
        joinRequest.setReviewerId(userId);
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequestMapper.updateById(joinRequest);

        if ("approve".equals(request.getAction())) {
            FamilyMember member = FamilyMember.builder()
                    .familyId(joinRequest.getFamilyId())
                    .userId(joinRequest.getApplicantUserId())
                    .name(joinRequest.getApplicantName())
                    .gender(Gender.male)
                    .isCreator(0)
                    .build();
            memberMapper.insert(member);
        }
    }

    private void handleEditRequest(EditRequest editRequest, HandleApprovalRequest request, Long userId) {
        RequestStatus newStatus = "approve".equals(request.getAction()) ? 
                RequestStatus.approved : RequestStatus.rejected;
        
        editRequest.setStatus(newStatus);
        editRequest.setReviewerId(userId);
        editRequest.setReviewedAt(LocalDateTime.now());
        editRequestMapper.updateById(editRequest);

        if ("approve".equals(request.getAction())) {
            FamilyMember member = memberMapper.selectById(editRequest.getMemberId());
            if (member != null) {
                switch (editRequest.getFieldName()) {
                    case "name" -> member.setName(editRequest.getNewValue());
                    case "avatar" -> member.setAvatar(editRequest.getNewValue());
                    case "bio" -> member.setBio(editRequest.getNewValue());
                }
                memberMapper.updateById(member);
            }
        }
    }

    @Override
    public void handleApprovalAdmin(Long familyId, Long requestId, HandleApprovalRequest request) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        if (request.getAction() == null || 
            (!request.getAction().equals("approve") && !request.getAction().equals("reject"))) {
            throw new BusinessException("action必须是approve或reject");
        }

        JoinRequest joinRequest = joinRequestMapper.selectById(requestId);
        if (joinRequest != null && joinRequest.getFamilyId().equals(familyId)) {
            handleJoinRequest(joinRequest, request, 1L);
            return;
        }

        EditRequest editRequest = editRequestMapper.selectById(requestId);
        if (editRequest != null && editRequest.getFamilyId().equals(familyId)) {
            handleEditRequest(editRequest, request, 1L);
            return;
        }

        throw new BusinessException("申请不存在");
    }
}
