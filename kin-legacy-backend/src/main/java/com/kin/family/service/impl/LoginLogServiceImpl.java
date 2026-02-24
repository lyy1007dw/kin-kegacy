package com.kin.family.service.impl;

import com.kin.family.entity.LoginLog;
import com.kin.family.mapper.LoginLogMapper;
import com.kin.family.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现
 *
 * @author candong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public void recordLoginLog(Long userId, String username, String loginType,
                              boolean success, String errorMsg, HttpServletRequest request) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginType(loginType);
            loginLog.setStatus(success ? 1 : 0);
            loginLog.setErrorMsg(errorMsg);
            loginLog.setCreateTime(LocalDateTime.now());

            if (request != null) {
                loginLog.setIpAddress(getIpAddress(request));
                loginLog.setUserAgent(request.getHeader("User-Agent"));
                loginLog.setDevice(getDevice(request.getHeader("User-Agent")));
            }

            log.info("准备记录登录日志: userId={}, username={}, type={}, success={}", userId, username, loginType, success);
            loginLogMapper.insert(loginLog);
            log.info("登录日志记录成功: id={}, userId={}, username={}", loginLog.getId(), userId, username);
        } catch (Exception e) {
            log.error("记录登录日志失败: userId={}, username={}, error={}", userId, username, e.getMessage(), e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getDevice(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("MicroMessenger")) {
            return "WeChat";
        }
        if (userAgent.contains("Mobile")) {
            return "Mobile";
        }
        return "PC";
    }
}
