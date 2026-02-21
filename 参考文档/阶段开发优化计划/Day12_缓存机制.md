# Day 12 - 缓存机制

**开发日期**：第 12 天
**优先级**：P3（较低）
**所属阶段**：体验优化

---

## 一、功能需求描述

### 1.1 背景

系统需要引入缓存机制，提升查询性能，减少数据库压力。

### 1.2 缓存场景

| 缓存场景 | 缓存策略 | 过期时间 |
|----------|----------|----------|
| 用户信息 | Redis | 30分钟 |
| 家谱树结构 | Redis | 变更时清除 |
| 系统配置 | Redis | 长期缓存 |
| 家谱基本信息 | Redis | 10分钟 |

---

## 二、Redis 配置

### 2.1 依赖引入

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### 2.2 配置

```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
    timeout: 3000ms
```

### 2.3 Redis 配置类

```java
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // JSON 序列化
        Jackson2JsonRedisSerializer<Object> serializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(), 
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        serializer.setObjectMapper(mapper);
        
        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value 使用 JSON 序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

---

## 三、缓存注解

### 3.1 自定义缓存注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Cacheable {
    String value() default "";           // 缓存名称
    String key() default "";              // 缓存Key，支持SpEL
    long expire() default 1800;           // 过期时间(秒)
    boolean condition() default true;     // 条件缓存
}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
    String value() default "";
    String key() default "";
    String[] keys() default {};           // 多个Key
    boolean allEntries() default false;   // 清除所有
    beforeInvocation() default true;
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CachePut {
    String value() default "";
    String key() default "";
    long expire() default 1800;
}
```

### 3.2 缓存切面

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Pointcut("@annotation(com.kin.family.annotation.Cacheable)")
    public void cacheablePointcut() {}
    
    @Pointcut("@annotation(com.kin.family.annotation.CacheEvict)")
    public void cacheEvictPointcut() {}
    
    @Around("cacheablePointcut()")
    public Object aroundCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String cacheName = cacheable.value();
        String key = getKey(joinPoint, cacheable.key());
        String cacheKey = cacheName + ":" + key;
        
        // 查询缓存
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            log.debug("缓存命中: {}", cacheKey);
            return cachedValue;
        }
        
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 存入缓存
        if (result != null && cacheable.condition()) {
            redisTemplate.opsForValue().set(
                cacheKey, 
                result, 
                cacheable.expire(), 
                TimeUnit.SECONDS
            );
            log.debug("缓存写入: {}", cacheKey);
        }
        
        return result;
    }
    
    @Around("cacheEvictPointcut()")
    public Object aroundCacheEvict(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        String cacheName = cacheEvict.value();
        
        // 执行方法前清除缓存
        if (cacheEvict.beforeInvocation()) {
            evictCache(cacheName, cacheEvict);
        }
        
        Object result = joinPoint.proceed();
        
        // 执行方法后清除缓存
        if (!cacheEvict.beforeInvocation()) {
            evictCache(cacheName, cacheEvict);
        }
        
        return result;
    }
    
    private void evictCache(String cacheName, CacheEvict cacheEvict) {
        if (cacheEvict.allEntries()) {
            Set<String> keys = redisTemplate.keys(cacheName + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } else if (cacheEvict.keys().length > 0) {
            for (String key : cacheEvict.keys()) {
                redisTemplate.delete(cacheName + ":" + key);
            }
        }
    }
    
    private String getKey(JoinPoint joinPoint, String keyPattern) {
        if (StringUtils.isBlank(keyPattern)) {
            // 默认使用方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getName();
        }
        
        // 支持 SpEL
        EvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature())
            .getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        
        return new SpelExpressionParser()
            .parseExpression(keyPattern)
            .getValue(context, String.class);
    }
}
```

---

## 四、应用场景

### 4.1 用户信息缓存

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;
    
    private static final String USER_CACHE = "user";
    private static final long USER_CACHE_EXPIRE = 1800; // 30分钟
    
    @Cacheable(value = USER_CACHE, key = "#userId", expire = USER_CACHE_EXPIRE)
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @CacheEvict(value = USER_CACHE, key = "#userId")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
    
    public void evictUserCache(Long userId) {
        redisTemplate.delete(USER_CACHE + ":" + userId);
    }
}
```

### 4.2 家谱树缓存

```java
@Service
@RequiredArgsConstructor
public class FamilyTreeService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRelationMapper relationMapper;
    
    private static final String FAMILY_TREE_CACHE = "family_tree";
    
    public List<TreeNodeVO> getFamilyTree(Long familyId) {
        String cacheKey = FAMILY_TREE_CACHE + ":" + familyId;
        
        // 查询缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (List<TreeNodeVO>) cached;
        }
        
        // 查询数据库
        List<TreeNodeVO> tree = buildFamilyTree(familyId);
        
        // 存入缓存（不设置过期时间，变更时主动清除）
        redisTemplate.opsForValue().set(cacheKey, tree);
        
        return tree;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long familyId, FamilyMember member) {
        // 业务逻辑
        memberMapper.insert(member);
        
        // 清除缓存
        evictFamilyTreeCache(familyId);
    }
    
    public void evictFamilyTreeCache(Long familyId) {
        String cacheKey = FAMILY_TREE_CACHE + ":" + familyId;
        redisTemplate.delete(cacheKey);
        log.info("清除家谱树缓存: {}", familyId);
    }
}
```

### 4.3 系统配置缓存

```java
@Service
@RequiredArgsConstructor
public class ConfigService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigMapper configMapper;
    
    private static final String CONFIG_CACHE = "system_config";
    private static final long CONFIG_CACHE_EXPIRE = 86400; // 24小时
    
    public String getConfigValue(String key) {
        String cacheKey = CONFIG_CACHE + ":" + key;
        
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (String) cached;
        }
        
        String value = configMapper.selectByKey(key);
        if (value != null) {
            redisTemplate.opsForValue().set(cacheKey, value, CONFIG_CACHE_EXPIRE, TimeUnit.SECONDS);
        }
        
        return value;
    }
    
    @CacheEvict(value = CONFIG_CACHE, allEntries = true)
    public void updateConfig(String key, String value) {
        configMapper.updateByKey(key, value);
    }
}
```

---

## 五、分布式锁

### 5.1 分布式锁注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {
    String key() default "";           // 锁Key
    long waitTime() default 0;          // 等待时间(毫秒)
    long leaseTime() default 30000;     // 释放时间(毫秒)
}
```

### 5.2 分布式锁实现

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LockAspect {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Around("@annotation(lock)")
    public Object aroundLock(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        String lockKey = "lock:" + lock.key();
        RLock rLock = new RedissonClient().getLock(lockKey);
        
        boolean acquired = rLock.tryLock(
            lock.waitTime(), 
            lock.leaseTime(), 
            TimeUnit.MILLISECONDS
        );
        
        if (!acquired) {
            throw new BusinessException("系统繁忙，请稍后重试");
        }
        
        try {
            return joinPoint.proceed();
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }
}
```

### 5.3 使用示例

```java
@Lock(key = "'family:' + #familyId + ':member:add'")
public Result<Void> addMember(AddMemberRequest request, Long familyId) {
    // 防止同一家谱重复添加
}
```

---

## 六

### 6.1 缓存统计

```java
、缓存监控@RestController
@RequestMapping("/api/admin/cache")
public class CacheController {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCacheStats() {
        Properties info = redisTemplate.getConnectionFactory()
            .getConnection().info();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("usedMemory", info.getProperty("used_memory_human"));
        stats.put("connectedClients", info.getProperty("connected_clients"));
        stats.put("totalKeys", redisTemplate.keys("*").size());
        
        return Result.success(stats);
    }
}
```

---

## 七、交付物

- [ ] Redis 配置
- [ ] Cacheable 缓存注解
- [ ] CacheEvict 清除缓存注解
- [ ] CacheAspect 缓存切面
- [ ] 用户信息缓存实现
- [ ] 家谱树缓存实现
- [ ] 系统配置缓存实现
- [ ] 分布式锁实现

---

## 八、注意事项

1. 缓存与数据库一致性
2. 缓存穿透（空值缓存、布隆过滤器）
3. 缓存雪崩（随机过期时间）
4. 缓存击穿（分布式锁）
5. 敏感数据不缓存
6. 定期清理无用缓存
