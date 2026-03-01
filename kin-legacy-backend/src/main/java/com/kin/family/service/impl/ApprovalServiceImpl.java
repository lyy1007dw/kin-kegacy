package com.kin.family.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kin.family.dto.ApprovalDetailDTO;
import com.kin.family.dto.ApprovalHandleDTO;
import com.kin.family.dto.MemberEditDTO;
import com.kin.family.dto.PageResult;
import com.kin.family.entity.EditRequest;
import com.kin.family.entity.Family;
import com.kin.family.entity.FamilyMember;
import com.kin.family.entity.JoinRequest;
import com.kin.family.constant.GenderEnum;
import com.kin.family.constant.RequestStatusEnum;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批服务实现
 *
 * @author candong
 */
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final JoinRequestMapper joinRequestMapper;
    private final EditRequestMapper editRequestMapper;
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApprovalMapper approvalMapper;

    @Override
    public PageResult<ApprovalDetailDTO> getApprovals(Long familyId, String type, String status, Integer page, Integer size) {
        Family family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家谱不存在");
        }

        Page<ApprovalDetailDTO> pageParam = new Page<>(page, size);
        List<ApprovalDetailDTO> records = approvalMapper.getApprovalsByFamilyId(pageParam, familyId, type, status);

        return PageResult.of(records, pageParam.getTotal(), page, size);
    }

    @Override
    public PageResult<ApprovalDetailDTO> getAllApprovals(String type, String status, Integer page, Integer size) {
        Page<ApprovalDetailDTO> pageParam = new Page<>(page, size);
        List<ApprovalDetailDTO> records = approvalMapper.getAllApprovals(pageParam, type, status);

        return PageResult.of(records, pageParam.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void handleApproval(Long familyId, Long requestId, ApprovalHandleDTO request, Long userId) {
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

    private void handleJoinRequest(JoinRequest joinRequest, ApprovalHandleDTO request, Long userId) {
        RequestStatusEnum newStatus = "approve".equals(request.getAction()) ?
                RequestStatusEnum.APPROVED : RequestStatusEnum.REJECTED;

        joinRequest.setStatus(newStatus);
        joinRequest.setReviewerId(userId);
        joinRequest.setReviewedAt(LocalDateTime.now());
        joinRequestMapper.updateById(joinRequest);

        if ("approve".equals(request.getAction())) {
            FamilyMember member = FamilyMember.builder()
                    .familyId(joinRequest.getFamilyId())
                    .userId(joinRequest.getApplicantUserId())
                    .name(joinRequest.getApplicantName())
                    .gender(GenderEnum.MALE)
                    .isCreator(0)
                    .build();
            memberMapper.insert(member);
        }
    }

    private void handleEditRequest(EditRequest editRequest, ApprovalHandleDTO request, Long userId) {
        RequestStatusEnum newStatus = "approve".equals(request.getAction()) ?
                RequestStatusEnum.APPROVED : RequestStatusEnum.REJECTED;

        editRequest.setStatus(newStatus);
        editRequest.setReviewerId(userId);
        editRequest.setReviewedAt(LocalDateTime.now());
        
        if ("reject".equals(request.getAction()) && request.getRemark() != null) {
            editRequest.setRejectReason(request.getRemark());
        }
        
        editRequestMapper.updateById(editRequest);

        if ("approve".equals(request.getAction())) {
            FamilyMember member = memberMapper.selectById(editRequest.getMemberId());
            if (member != null && editRequest.getChangesJson() != null) {
                try {
                    Map<String, MemberEditDTO.FieldChange> changes = objectMapper.readValue(
                            editRequest.getChangesJson(),
                            new TypeReference<Map<String, MemberEditDTO.FieldChange>>() {}
                    );
                    
                    for (Map.Entry<String, MemberEditDTO.FieldChange> entry : changes.entrySet()) {
                        String fieldName = entry.getKey();
                        String newValue = entry.getValue().getNewValue();
                        applyFieldChange(member, fieldName, newValue);
                    }
                    memberMapper.updateById(member);
                } catch (JsonProcessingException e) {
                    throw new BusinessException("解析修改内容失败");
                }
            }
        }
    }
    
    private void applyFieldChange(FamilyMember member, String fieldName, String newValue) {
        if (newValue == null) return;
        
        switch (fieldName) {
            case "name" -> member.setName(newValue);
            case "avatar" -> member.setAvatar(newValue);
            case "birthDate" -> member.setBirthDate(LocalDate.parse(newValue));
            case "birthPlace" -> member.setBirthPlace(newValue);
            case "deathDate" -> member.setDeathDate(LocalDate.parse(newValue));
            case "bio" -> member.setBio(newValue);
        }
    }

    @Override
    public void handleApprovalAdmin(Long familyId, Long requestId, ApprovalHandleDTO request) {
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
