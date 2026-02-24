package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kin.family.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 *
 * @author candong
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
