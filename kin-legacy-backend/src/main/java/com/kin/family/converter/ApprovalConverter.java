package com.kin.family.converter;

import com.kin.family.dto.ApprovalDetailDTO;
import com.kin.family.entity.EditRequest;
import com.kin.family.entity.JoinRequest;
import org.springframework.stereotype.Component;

/**
 * 审批转换器
 *
 * @author candong
 */
@Component
public class ApprovalConverter {

    /**
     * JoinRequest转DetailDTO
     */
    public ApprovalDetailDTO toDetailDTO(JoinRequest joinRequest) {
        if (joinRequest == null) {
            return null;
        }
        ApprovalDetailDTO dto = new ApprovalDetailDTO();
        dto.setId(joinRequest.getId());
        dto.setType("join");
        dto.setFamilyId(joinRequest.getFamilyId());
        dto.setApplicantUserId(joinRequest.getApplicantUserId());
        dto.setApplicantName(joinRequest.getApplicantName());
        dto.setRelationDesc(joinRequest.getRelationDesc());
        dto.setStatus(joinRequest.getStatus());
        dto.setCreateTime(joinRequest.getCreateTime());
        return dto;
    }

    /**
     * EditRequest转DetailDTO
     */
    public ApprovalDetailDTO toDetailDTO(EditRequest editRequest) {
        if (editRequest == null) {
            return null;
        }
        ApprovalDetailDTO dto = new ApprovalDetailDTO();
        dto.setId(editRequest.getId());
        dto.setType("edit");
        dto.setFamilyId(editRequest.getFamilyId());
        dto.setApplicantUserId(editRequest.getApplicantUserId());
        dto.setMemberId(editRequest.getMemberId());
        dto.setFieldName(editRequest.getFieldName());
        dto.setOldValue(editRequest.getOldValue());
        dto.setNewValue(editRequest.getNewValue());
        dto.setStatus(editRequest.getStatus());
        dto.setCreateTime(editRequest.getCreateTime());
        return dto;
    }
}
