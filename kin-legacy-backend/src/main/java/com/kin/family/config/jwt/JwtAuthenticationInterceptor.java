package com.kin.family.config.jwt;

import com.kin.family.annotation.Logical;
import com.kin.family.annotation.RequireAdmin;
import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.exception.ForbiddenException;
import com.kin.family.exception.UnauthorizedException;
import com.kin.family.service.UserRoleService;
import com.kin.family.util.JwtUtil;
import com.kin.family.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JWT认证拦截器
 *
 * @author candong
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final UserRoleService userRoleService;

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("/api/[^/]+/\\{([^}]+)\\}");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (!handlerMethod.hasMethodAnnotation(RequireLogin.class)) {
            RequireLogin classLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
            if (classLogin == null) {
                return true;
            }
        }

        String authHeader = request.getHeader(jwtProperties.getHeader());
        if (authHeader == null || !authHeader.startsWith(jwtProperties.getPrefix())) {
            throw new UnauthorizedException("未登录或Token无效");
        }

        String token = authHeader.substring(jwtProperties.getPrefix().length());

        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token已过期或无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String globalRole = jwtUtil.getGlobalRoleFromToken(token);

        if (globalRole == null) {
            throw new UnauthorizedException("Token版本过期，请重新登录");
        }

        UserContextUtil.setUserId(userId);
        UserContextUtil.setUsername(username);
        UserContextUtil.setGlobalRole(globalRole);

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null) {
            checkRole(globalRole, requireRole);
        }

        RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        if (requireAdmin != null) {
            checkAdminPermission(userId, request, requireAdmin);
        }

        return true;
    }

    private void checkRole(String globalRole, RequireRole requireRole) {
        String[] roles = requireRole.value();
        if (roles.length == 0) {
            return;
        }

        boolean hasRole = false;
        if (requireRole.logical() == Logical.OR) {
            hasRole = Arrays.asList(roles).contains(globalRole);
        } else {
            hasRole = Arrays.asList(roles).contains(globalRole);
        }

        if (!hasRole) {
            throw new ForbiddenException("权限不足");
        }
    }

    private void checkAdminPermission(Long userId, HttpServletRequest request, RequireAdmin requireAdmin) {
        String globalRole = UserContextUtil.getGlobalRole();

        if ("SUPER_ADMIN".equals(globalRole)) {
            return;
        }

        String familyIdParam = requireAdmin.familyIdParam();
        String familyIdStr = request.getParameter(familyIdParam);
        if (familyIdStr == null) {
            familyIdStr = extractPathVariable(request, familyIdParam);
        }

        if (familyIdStr == null) {
            throw new ForbiddenException("缺少家谱ID参数");
        }

        Long familyId;
        try {
            familyId = Long.parseLong(familyIdStr);
        } catch (NumberFormatException e) {
            throw new ForbiddenException("无效的家谱ID");
        }

        if (!userRoleService.isAdminOfGenealogy(userId, familyId)) {
            throw new ForbiddenException("您不是该家谱的管理员");
        }
    }

    private String extractPathVariable(HttpServletRequest request, String paramName) {
        String uri = request.getRequestURI();
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(uri);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (paramName.equals(varName)) {
                return matcher.group(0).replace("/api/", "").replace("{", "").replace("}", "");
            }
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextUtil.clear();
    }
}
