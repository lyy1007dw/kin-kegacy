# Day 9 - 异常处理完善与日志系统

**开发日期**：第 9 天
**优先级**：P2（中等）
**所属阶段**：质量提升

---

## 一、功能需求描述

### 1.1 背景

系统需要完善的异常处理机制和日志系统，便于问题排查和系统监控。

### 1.2 目标

- 定义统一业务异常码
- 完善异常信息
- 前端友好的错误提示
- 搭建操作日志系统
- 搭建访问日志系统

---

## 二、异常处理完善

### 2.1 异常码定义

```java
public enum ErrorCode {
    // 通用错误 1000-1999
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    
    // 认证错误 2000-2999
    UNAUTHORIZED(401, "未登录或Token已过期"),
    TOKEN_INVALID(401, "Token无效"),
    TOKEN_EXPIRED(401, "Token已过期"),
    FORBIDDEN(403, "权限不足"),
    USER_DISABLED(403, "账号已被禁用"),
    ACCOUNT_LOCKED(423, "账号已被锁定"),
    
    // 用户错误 3000-3999
    USER_NOT_FOUND(404, "用户不存在"),
    USERNAME_EXISTS(400, "用户名已存在"),
    PHONE_EXISTS(400, "手机号已被注册"),
    PASSWORD_ERROR(401, "用户名或密码错误"),
    LOGIN_FAIL_LOCKED(423, "登录失败次数过多，账号已锁定"),
    
    // 家谱错误 4000-4999
    FAMILY_NOT_FOUND(404, "家谱不存在"),
    FAMILY_NAME_EXISTS(400, "家谱名称已存在"),
    ONLY_ADMIN_CANNOT_DELETE(400, "该用户是该家谱的唯一管理员"),
    NOT_FAMILY_ADMIN(403, "您不是该家谱的管理员"),
    
    // 成员错误 5000-5999
    MEMBER_NOT_FOUND(404, "成员不存在"),
    MEMBER_NAME_EXISTS(400, "成员姓名已存在"),
    RELATION_INVALID(400, "关系设置无效"),
    
    // 审批错误 6000-6999
    APPROVAL_NOT_FOUND(404, "审批记录不存在"),
    APPROVAL_EXPIRED(400, "审批已过期"),
    CANNOT_APPROVE_SELF(400, "不能审批自己的申请");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

### 2.2 统一响应结果

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    public static <T> Result<T> success() {
        return Result.<T>builder()
            .code(200)
            .message("success")
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(200)
            .message("success")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> Result<T> error(ErrorCode errorCode) {
        return Result.<T>builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

### 2.3 全局异常处理

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("参数校验异常: {}", message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("绑定异常: {}", message);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 认证异常
     */
    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public Result<Void> handleUnauthorizedException(Exception e) {
        log.warn("认证异常: {}", e.getMessage());
        return Result.error(ErrorCode.UNAUTHORIZED.getCode(), e.getMessage());
    }
    
    /**
     * 权限异常
     */
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public Result<Void> handleForbiddenException(Exception e) {
        log.warn("权限异常: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getCode(), e.getMessage());
    }
    
    /**
     * 资源不存在
     */
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getCode(), e.getMessage());
    }
    
    /**
     * 数据库异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityException(DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), "数据操作异常，请检查输入");
    }
    
    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
```

### 2.4 自定义异常类

```java
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
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(404, message);
    }
}
```

---

## 三、日志系统

### 3.1 日志配置

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.kin.family: DEBUG
    org.springframework.web: INFO
    com.kin.family.mapper: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/kin-legacy.log
    max-size: 100MB
    max-history: 30
```

### 3.2 操作日志表

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
  device VARCHAR(200) COMMENT '设备信息',
  status TINYINT COMMENT '状态: 0失败 1成功',
  error_msg TEXT COMMENT '错误信息',
  duration BIGINT COMMENT '执行时长(ms)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_module (module),
  INDEX idx_created_at (created_at)
) COMMENT '操作日志表';
```

### 3.3 操作日志实体

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> requestParams;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> responseResult;
    private String ip;
    private String location;
    private String device;
    private Integer status;
    private String errorMsg;
    private Long duration;
    private LocalDateTime createdAt;
}
```

### 3.4 操作日志注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String module();      // 模块名称
    String operation();   // 操作名称
    boolean saveParams() default true;    // 是否保存请求参数
    boolean saveResult() default false;   // 是否保存响应结果
}
```

### 3.5 操作日志切面

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    @Pointcut("@annotation(com.kin.family.annotation.OperationLog)")
    public void logPointcut() {}
    
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 构建日志对象
        OperationLogEntity logEntity = buildLogEntity(joinPoint, operationLog);
        
        // 记录请求参数
        if (operationLog.saveParams()) {
            logEntity.setRequestParams(getRequestParams(joinPoint));
        }
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            logEntity.setStatus(1);
            if (operationLog.saveResult()) {
                logEntity.setResponseResult(convertToMap(result));
            }
            
            return result;
        } catch (Exception e) {
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            logEntity.setDuration(System.currentTimeMillis() - startTime);
            // 异步保存日志
            CompletableFuture.runAsync(() -> {
                try {
                    operationLogMapper.insert(logEntity);
                } catch (Exception e) {
                    log.error("保存操作日志失败", e);
                }
            });
        }
    }
    
    private OperationLogEntity buildLogEntity(JoinPoint joinPoint, OperationLog operationLog) {
        OperationLogEntity log = new OperationLogEntity();
        log.setUserId(UserContext.getUserId());
        log.setUsername(UserContext.getUsername());
        log.setModule(operationLog.module());
        log.setOperation(operationLog.operation());
        log.setMethod(joinPoint.getSignature().toShortString());
        log.setIpAddress(UserContext.getIpAddress());
        log.setRequestUrl(getRequestUrl());
        log.setRequestMethod(getRequestMethod());
        log.setDevice(getDeviceInfo());
        return log;
    }
}
```

### 3.6 使用示例

```java
@OperationLog(module = "用户管理", operation = "修改用户角色")
@RequireRole("SUPER_ADMIN")
@PutMapping("/admin/user/{userId}/role")
public Result<Void> updateUserRole(
        @PathVariable Long userId, 
        @RequestBody UpdateRoleRequest request) {
    userRoleService.updateUserRole(userId, request.getGenealogyId(), request.getRole());
    return Result.success();
}

@OperationLog(module = "家谱管理", operation = "创建家谱", saveParams = true)
@PostMapping("/family")
public Result<Family> createFamily(@RequestBody CreateFamilyRequest request) {
    Family family = familyService.createFamily(request, UserContext.getUserId());
    return Result.success(family);
}

@OperationLog(module = "成员管理", operation = "删除成员")
@DeleteMapping("/family/{familyId}/member/{memberId}")
public Result<Void> deleteMember(
        @PathVariable Long familyId, 
        @PathVariable Long memberId) {
    memberService.deleteMember(memberId, familyId);
    return Result.success();
}
```

### 3.7 访问日志

```java
@Component
@RequiredArgsConstructor
public class AccessLogInterceptor implements HandlerInterceptor {
    
    private final AccessLogService accessLogService;
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            AccessLog log = AccessLog.builder()
                .userId(UserContext.getUserId())
                .ip(getClientIp(request))
                .requestUrl(request.getRequestURI())
                .requestMethod(request.getMethod())
                .httpStatus(response.getStatus())
                .duration(System.currentTimeMillis() - (Long) request.getAttribute("startTime"))
                .userAgent(request.getHeader("User-Agent"))
                .build();
            
            // 异步保存
            CompletableFuture.runAsync(() -> accessLogService.save(log));
        }
    }
}
```

---

## 四、日志分析

### 4.1 ELK 日志收集（可选）

```yaml
# logback-spring.xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5044</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"app":"kin-legacy"}</customFields>
    </encoder>
</appender>
```

### 4.2 日志查询

```sql
-- 查询错误日志
SELECT * FROM operation_log 
WHERE status = 0 
ORDER BY created_at DESC 
LIMIT 100;

-- 查询用户操作记录
SELECT * FROM operation_log 
WHERE user_id = 1 
AND module = '家谱管理' 
ORDER BY created_at DESC;

-- 查询慢请求
SELECT * FROM operation_log 
WHERE duration > 3000 
ORDER BY duration DESC;
```

---

## 五、交付物

- [ ] ErrorCode 枚举类
- [ ] Result 统一响应类
- [ ] BusinessException 及子类
- [ ] GlobalExceptionHandler 全局异常处理
- [ ] OperationLog 注解和切面
- [ ] AccessLogInterceptor 访问日志
- [ ] 操作日志表 SQL

---

## 六、注意事项

1. 敏感信息（密码、Token）不记录日志
2. 日志异步写入避免影响性能
3. 定期清理历史日志
4. 生产环境日志级别调整
