# Day 4 - 权限控制完善

**开发日期**：第 4 天
**优先级**：P0（高）
**所属阶段**：安全加固

---

## 一、功能需求描述

### 1.1 背景

系统需要实现基于角色的访问控制（RBAC），确保不同角色的用户只能访问其权限范围内的功能和数据。

### 1.2 功能目标

- 实现权限注解 `@RequireRole`
- 实现权限拦截器
- 完善数据权限控制
- 添加操作日志审计
- 权限校验统一异常处理

### 1.3 权限矩阵

| 功能模块 | 超级管理员 | 家谱管理员 | 普通用户 |
|----------|:----------:|:----------:|:--------:|
| 后台登录 | ✅ | ❌ | ❌ |
| 家谱列表（全部） | ✅ | ❌ | ❌ |
| 家谱详情（全部） | ✅ | ❌ | ❌ |
| 家谱管理（自己的） | ❌ | ✅ | ❌ |
| 用户管理 | ✅ | ❌ | ❌ |
| 成员管理（全部） | ✅ | ❌ | ❌ |
| 成员管理（本家谱） | ❌ | ✅ | ❌ |
| 审批管理（全部） | ✅ | ❌ | ❌ |
| 审批管理（本家谱） | ❌ | ✅ | ❌ |
| 查看家谱树 | ✅ | ✅ | ✅ |

---

## 二、权限注解设计

### 2.1 注解定义

#### @RequireLogin

标记需要登录的接口：

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
```

#### @RequireRole

标记需要特定角色的接口：

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {};  // 允许的角色列表
    Logical logical() default Logical.OR;  // OR/AND
}

public enum Logical {
    OR, AND
}
```

#### @RequireAdmin

标记需要家谱管理员权限的接口：

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
    String familyIdParam() default "familyId";  // 家谱ID参数名
}
```

### 2.2 使用示例

```java
// 需要登录
@RequireLogin
@GetMapping("/user/info")
public Result<UserInfo> getUserInfo() { ... }

// 需要超级管理员角色
@RequireRole("SUPER_ADMIN")
@GetMapping("/admin/users")
public Result<List<User>> listUsers() { ... }

// 需要家谱管理员或超级管理员
@RequireRole({"SUPER_ADMIN", "GENEALOGY_ADMIN"})
@PostMapping("/family/{familyId}/member")
public Result<Void> addMember(@PathVariable Long familyId, ...) { ... }

// 需要特定家谱的管理员权限
@RequireAdmin
@PutMapping("/family/{familyId}/member/{memberId}")
public Result<Void> updateMember(@PathVariable Long familyId, ...) { ... }
```

---

## 三、权限拦截器实现

### 3.1 AuthInterceptor

```java
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final UserRoleService userRoleService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 获取注解
        RequireLogin requireLogin = method.getAnnotation(RequireLogin.class);
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        RequireAdmin requireAdmin = method.getAnnotation(RequireAdmin.class);
        
        if (requireLogin == null && requireRole == null && requireAdmin == null) {
            requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        
        // 无权限注解，放行
        if (requireLogin == null && requireRole == null && requireAdmin == null) {
            return true;
        }
        
        // 获取Token
        String token = extractToken(request);
        if (token == null) {
            throw new UnauthorizedException("未登录或Token已过期");
        }
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token无效或已过期");
        }
        
        // 解析用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String globalRole = jwtUtil.getRoleFromToken(token);
        
        // 存入上下文
        UserContext.setUserId(userId);
        UserContext.setGlobalRole(globalRole);
        
        // 检查角色权限
        if (requireRole != null) {
            checkRole(globalRole, requireRole);
        }
        
        // 检查家谱管理员权限
        if (requireAdmin != null) {
            checkAdminPermission(userId, request, requireAdmin);
        }
        
        return true;
    }
    
    private void checkRole(String globalRole, RequireRole requireRole) {
        String[] roles = requireRole.value();
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
        String globalRole = UserContext.getGlobalRole();
        
        // 超级管理员直接放行
        if ("SUPER_ADMIN".equals(globalRole)) {
            return;
        }
        
        // 获取家谱ID
        String familyIdParam = requireAdmin.familyIdParam();
        String familyIdStr = request.getParameter(familyIdParam);
        if (familyIdStr == null) {
            familyIdStr = extractPathVariable(request, familyIdParam);
        }
        
        if (familyIdStr == null) {
            throw new BadRequestException("缺少家谱ID参数");
        }
        
        Long familyId = Long.parseLong(familyIdStr);
        
        // 检查是否为该家谱管理员
        if (!userRoleService.isAdminOfGenealogy(userId, familyId)) {
            throw new ForbiddenException("您不是该家谱的管理员");
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
```

### 3.2 WebMvcConfig 配置

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/login",
                "/api/auth/wx-login",
                "/api/auth/refresh",
                "/api/auth/captcha"
            );
    }
}
```

---

## 四、数据权限控制

### 4.1 数据权限切面

```java
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {
    
    private final UserRoleService userRoleService;
    
    /**
     * 家谱数据权限校验
     */
    @Before("@annotation(requireAdmin)")
    public void checkDataPermission(JoinPoint joinPoint, RequireAdmin requireAdmin) {
        Long userId = UserContext.getUserId();
        String globalRole = UserContext.getGlobalRole();
        
        // 超级管理员跳过校验
        if ("SUPER_ADMIN".equals(globalRole)) {
            return;
        }
        
        // 从方法参数中获取家谱ID
        Long familyId = extractFamilyId(joinPoint, requireAdmin.familyIdParam());
        if (familyId == null) {
            return;
        }
        
        // 校验权限
        if (!userRoleService.isAdminOfGenealogy(userId, familyId)) {
            throw new ForbiddenException("无权操作该家谱数据");
        }
    }
    
    private Long extractFamilyId(JoinPoint joinPoint, String paramName) {
        // 从方法参数中提取familyId
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    return Long.parseLong((String) arg);
                }
            }
        }
        return null;
    }
}
```

### 4.2 MyBatis 数据权限插件

```java
@Interceptor签名({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
@Component
public class DataPermissionInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        
        // 获取SQL
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");
        
        // 获取当前用户信息
        Long userId = UserContext.getUserId();
        String globalRole = UserContext.getGlobalRole();
        
        // 如果不是超级管理员，添加数据权限过滤
        if (!"SUPER_ADMIN".equals(globalRole) && sql.contains("SELECT")) {
            // 对特定表添加权限过滤
            sql = addDataPermission(sql, userId, globalRole);
            metaObject.setValue("delegate.boundSql.sql", sql);
        }
        
        return invocation.proceed();
    }
    
    private String addDataPermission(String sql, Long userId, String globalRole) {
        // 家谱管理员只能看自己的家谱
        if (sql.contains("FROM family") && !sql.contains("user_genealogy")) {
            sql = sql.replace("FROM family", 
                "FROM family f LEFT JOIN user_genealogy ug ON f.id = ug.genealogy_id WHERE ug.user_id = " + userId);
        }
        return sql;
    }
}
```

---

## 五、操作日志审计

### 5.1 操作日志表

```sql
CREATE TABLE operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '操作用户ID',
  username VARCHAR(50) COMMENT '操作用户名',
  module VARCHAR(50) COMMENT '模块',
  operation VARCHAR(50) COMMENT '操作',
  method VARCHAR(200) COMMENT '方法名',
  request_url VARCHAR(500) COMMENT '请求URL',
  request_method VARCHAR(10) COMMENT '请求方法',
  request_params TEXT COMMENT '请求参数',
  response_result TEXT COMMENT '响应结果',
  ip VARCHAR(50) COMMENT 'IP地址',
  location VARCHAR(100) COMMENT '操作地点',
  status TINYINT COMMENT '状态: 0失败 1成功',
  error_msg TEXT COMMENT '错误信息',
  duration BIGINT COMMENT '执行时长(ms)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_module (module),
  INDEX idx_created_at (created_at)
) COMMENT '操作日志表';
```

### 5.2 操作日志注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();      // 模块名称
    String operation();   // 操作名称
    boolean saveParams() default true;    // 是否保存请求参数
    boolean saveResult() default false;   // 是否保存响应结果
}
```

### 5.3 操作日志切面

```java
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 构建日志对象
        OperationLogEntity log = new OperationLogEntity();
        log.setUserId(UserContext.getUserId());
        log.setUsername(UserContext.getUsername());
        log.setModule(operationLog.module());
        log.setOperation(operationLog.operation());
        log.setIpAddress(UserContext.getIpAddress());
        log.setRequestUrl(getRequestUrl());
        log.setRequestMethod(getRequestMethod());
        
        // 记录请求参数
        if (operationLog.saveParams()) {
            log.setRequestParams(getRequestParams(joinPoint));
        }
        
        try {
            Object result = joinPoint.proceed();
            
            log.setStatus(1);
            if (operationLog.saveResult()) {
                log.setResponseResult(JSON.toJSONString(result));
            }
            
            return result;
        } catch (Exception e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            log.setDuration(System.currentTimeMillis() - startTime);
            operationLogMapper.insert(log);
        }
    }
}
```

### 5.4 使用示例

```java
@OperationLog(module = "用户管理", operation = "修改用户角色")
@RequireRole("SUPER_ADMIN")
@PutMapping("/admin/user/{userId}/role")
public Result<Void> updateUserRole(@PathVariable Long userId, @RequestBody UpdateRoleRequest request) {
    // ...
}
```

---

## 六、统一异常处理

### 6.1 异常类定义

```java
// 基础业务异常
public class BusinessException extends RuntimeException {
    private final int code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// 未授权异常
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}

// 禁止访问异常
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}

// 资源未找到异常
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(404, message);
    }
}
```

### 6.2 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> handleForbidden(ForbiddenException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

---

## 七、接口改造

### 7.1 需要添加权限注解的接口

| Controller | 接口 | 权限注解 |
|------------|------|----------|
| AuthController | GET /api/auth/user-info | @RequireLogin |
| FamilyController | GET /api/family | @RequireLogin |
| FamilyController | POST /api/family | @RequireLogin |
| FamilyController | PUT /api/family/{id} | @RequireAdmin |
| FamilyController | DELETE /api/family/{id} | @RequireAdmin |
| MemberController | GET /api/family/{familyId}/member | @RequireLogin |
| MemberController | POST /api/family/{familyId}/member | @RequireAdmin |
| MemberController | PUT /api/family/{familyId}/member/{id} | @RequireAdmin |
| MemberController | DELETE /api/family/{familyId}/member/{id} | @RequireAdmin |
| ApprovalController | GET /api/family/{familyId}/approvals | @RequireAdmin |
| ApprovalController | POST /api/family/{familyId}/approvals/handle | @RequireAdmin |
| UserController | GET /api/admin/user | @RequireRole("SUPER_ADMIN") |
| AdminFamilyController | GET /api/admin/family | @RequireRole("SUPER_ADMIN") |

---

## 八、测试用例

### 8.1 权限拦截测试

| 测试项 | 请求 | 预期结果 |
|--------|------|----------|
| 无Token访问 | GET /api/auth/user-info | 401 未登录 |
| 无效Token访问 | GET /api/auth/user-info (无效Token) | 401 Token无效 |
| 普通用户访问管理员接口 | GET /api/admin/user (普通用户Token) | 403 权限不足 |
| 家谱管理员访问其他家谱 | PUT /api/family/999 (无权限的家谱) | 403 不是管理员 |
| 超级管理员访问所有 | GET /api/admin/user (超管Token) | 200 正常返回 |

---

## 九、交付物

- [ ] @RequireLogin 注解
- [ ] @RequireRole 注解
- [ ] @RequireAdmin 注解
- [ ] @OperationLog 注解
- [ ] AuthInterceptor 权限拦截器
- [ ] DataPermissionAspect 数据权限切面
- [ ] OperationLogAspect 操作日志切面
- [ ] GlobalExceptionHandler 全局异常处理
- [ ] 各 Controller 接口权限注解添加
- [ ] 单元测试代码
