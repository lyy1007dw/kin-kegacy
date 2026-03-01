package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kin.family.dto.MemberQueryRequest;
import com.kin.family.dto.MemberVO;
import com.kin.family.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 家族成员Mapper
 *
 * @author candong
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
    IPage<MemberVO> selectMemberPage(Page<?> page, @Param("query") MemberQueryRequest query);
}
