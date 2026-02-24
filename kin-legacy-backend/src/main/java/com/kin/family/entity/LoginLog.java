package com.kin.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author candong
 */
@Data
@TableName("login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("login_type")
    private String loginType;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("location")
    private String location;

    @TableField("device")
    private String device;

    @TableField("user_agent")
    private String userAgent;

    @TableField("status")
    private Integer status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("create_time")
    private LocalDateTime createTime;
}
