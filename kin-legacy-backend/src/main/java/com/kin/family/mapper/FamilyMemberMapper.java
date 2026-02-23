package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kin.family.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家族成员Mapper
 *
 * @author candong
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
}
