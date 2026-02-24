package com.kin.family.config.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.entity.OperationLog;
import com.kin.family.mapper.OperationLogMapper;
import com.kin.family.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面
 *
 * @author candong
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLogger)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLogger operationLogger) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLog operationLogEntity = new OperationLog();
        operationLogEntity.setUserId(UserContextUtil.getUserId());
        operationLogEntity.setUsername(UserContextUtil.getUsername());
        operationLogEntity.setModule(operationLogger.module());
        operationLogEntity.setOperation(operationLogger.operation());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLogEntity.setMethod(joinPoint.getSignature().toShortString());
            operationLogEntity.setRequestUrl(request.getRequestURI());
            operationLogEntity.setRequestMethod(request.getMethod());
            operationLogEntity.setIpAddress(getIpAddress(request));

            if (operationLogger.saveParams()) {
                operationLogEntity.setRequestParams(getRequestParams(joinPoint));
            }
        }

        try {
            Object result = joinPoint.proceed();

            operationLogEntity.setStatus(1);
            if (operationLogger.saveResult()) {
                try {
                    operationLogEntity.setResponseResult(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.warn("序列化响应结果失败", e);
                }
            }

            return result;
        } catch (Exception e) {
            operationLogEntity.setStatus(0);
            operationLogEntity.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operationLogEntity.setDuration(System.currentTimeMillis() - startTime);
            try {
                operationLogMapper.insert(operationLogEntity);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
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

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getParameterNames(joinPoint);
            if (paramNames == null || args == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof HttpServletRequest) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(paramNames[i]).append("=").append(objectMapper.writeValueAsString(args[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return null;
        }
    }

    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        try {
            org.aspectj.lang.reflect.MethodSignature signature = (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            return signature.getParameterNames();
        } catch (Exception e) {
            return null;
        }
    }
}
