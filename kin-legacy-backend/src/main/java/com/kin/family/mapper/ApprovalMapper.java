package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.ApprovalResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalMapper extends BaseMapper<ApprovalResponse> {
    List<ApprovalResponse> getApprovalsByFamilyId(Page<ApprovalResponse> page, @Param("familyId") Long familyId, @Param("type") String type, @Param("status") String status);
    List<ApprovalResponse> getAllApprovals(Page<ApprovalResponse> page, @Param("type") String type, @Param("status") String status);
}
