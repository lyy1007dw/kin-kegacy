package com.kin.family.service;

/**
 * 登录日志服务
 *
 * @author candong
 */
public interface LoginLogService {

    /**
     * 记录登录日志
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param loginType  登录类型
     * @param success   是否成功
     * @param errorMsg   错误信息
     * @param request   HttpServletRequest
     */
    void recordLoginLog(Long userId, String username, String loginType, 
                       boolean success, String errorMsg, jakarta.servlet.http.HttpServletRequest request);
}
