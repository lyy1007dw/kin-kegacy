package com.kin.family.config.aspect;

import com.kin.family.annotation.RequireAdmin;
import com.kin.family.exception.ForbiddenException;
import com.kin.family.service.UserRoleService;
import com.kin.family.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限切面
 *
 * @author candong
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {

    private final UserRoleService userRoleService;

    @Before("@annotation(requireAdmin)")
    public void checkDataPermission(JoinPoint joinPoint, RequireAdmin requireAdmin) {
        Long userId = UserContextUtil.getUserId();
        String globalRole = UserContextUtil.getGlobalRole();

        if ("SUPER_ADMIN".equals(globalRole)) {
            return;
        }

        Long familyId = extractFamilyId(joinPoint, requireAdmin.familyIdParam());
        if (familyId == null) {
            return;
        }

        if (!userRoleService.isAdminOfGenealogy(userId, familyId)) {
            throw new ForbiddenException("无权操作该家谱数据");
        }
    }

    private Long extractFamilyId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
