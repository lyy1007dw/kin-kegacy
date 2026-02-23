package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.ApprovalDetailDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批Mapper
 *
 * @author candong
 */
@Mapper
public interface ApprovalMapper extends BaseMapper<ApprovalDetailDTO> {
    List<ApprovalDetailDTO> getApprovalsByFamilyId(Page<ApprovalDetailDTO> page, @Param("familyId") Long familyId, @Param("type") String type, @Param("status") String status);
    List<ApprovalDetailDTO> getAllApprovals(Page<ApprovalDetailDTO> page, @Param("type") String type, @Param("status") String status);
}
