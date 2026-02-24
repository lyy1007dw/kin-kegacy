package com.kin.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kin.family.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author candong
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
