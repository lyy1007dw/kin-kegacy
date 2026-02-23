package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kin.family.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author candong
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
