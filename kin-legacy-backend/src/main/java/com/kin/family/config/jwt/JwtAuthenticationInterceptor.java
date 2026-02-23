package com.kin.family.config.jwt;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.util.jwt.JwtUtil;
import com.kin.family.util.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (!handlerMethod.hasMethodAnnotation(RequireLogin.class)) {
            return true;
        }

        String authHeader = request.getHeader(jwtProperties.getHeader());
        if (authHeader == null || !authHeader.startsWith(jwtProperties.getPrefix())) {
            throw new com.kin.family.exception.BusinessException(401, "未登录或Token无效");
        }

        String token = authHeader.substring(jwtProperties.getPrefix().length());

        if (!jwtUtil.validateToken(token)) {
            throw new com.kin.family.exception.BusinessException(401, "Token已过期或无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        UserContext.setUserId(userId);
        UserContext.setUsername(username);
        UserContext.setRole(role);

        if (handlerMethod.hasMethodAnnotation(RequireRole.class)) {
            String requiredRole = handlerMethod.getMethodAnnotation(RequireRole.class).value();
            if (!requiredRole.equals(role)) {
                throw new com.kin.family.exception.BusinessException(403, "权限不足");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
