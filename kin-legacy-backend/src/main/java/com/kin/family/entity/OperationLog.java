package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author candong
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String operation;

    private String method;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseResult;

    private String ipAddress;

    private String location;

    private Integer status;

    private String errorMsg;

    private Long duration;

    private LocalDateTime createTime;
}
