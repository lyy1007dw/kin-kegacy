# Day 5 - 接口安全加固与成员管理优化

**开发日期**：第 5 天
**优先级**：P0（高）
**所属阶段**：安全加固 + 功能完善

---

## 一、功能需求描述

### 1.1 接口安全加固

- 添加请求频率限制
- 敏感操作二次验证
- XSS/CSRF 防护
- 参数校验增强

### 1.2 成员管理模块优化

- 成员列表分页查询增强
- 支持多条件筛选
- 成员编辑功能增强
- 支持修改所属家谱

---

## 二、接口安全加固

### 2.1 请求频率限制

#### 实现方案

使用 Redis + 注解实现接口限流：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int value() default 10;        // 限制次数
    int duration() default 60;     // 时间窗口(秒)
    RateLimitType type() default RateLimitType.IP;  // 限流类型
}

public enum RateLimitType {
    IP,      // 按IP限流
    USER,    // 按用户限流
    ALL      // 全局限流
}
```

#### 限流切面

```java
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final StringRedisTemplate redisTemplate;
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.duration(), TimeUnit.SECONDS);
        }
        
        if (count > rateLimit.value()) {
            throw new BusinessException("请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
    
    private String buildKey(JoinPoint joinPoint, RateLimit rateLimit) {
        String prefix = "rate_limit:";
        switch (rateLimit.type()) {
            case IP:
                return prefix + "ip:" + UserContext.getIpAddress();
            case USER:
                return prefix + "user:" + UserContext.getUserId();
            default:
                return prefix + "all:" + joinPoint.getSignature().getName();
        }
    }
}
```

#### 使用示例

```java
@RateLimit(value = 5, duration = 60, type = RateLimitType.IP)
@PostMapping("/auth/login")
public Result<LoginResponse> login(@RequestBody LoginRequest request) { ... }

@RateLimit(value = 10, duration = 60, type = RateLimitType.USER)
@PostMapping("/family/{familyId}/member")
public Result<Void> addMember(...) { ... }
```

### 2.2 敏感操作二次验证

#### 二次验证注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecondAuth {
    String message() default "该操作需要二次验证";
}
```

#### 验证流程

```
1. 前端发起敏感操作请求
2. 后端返回需要二次验证标识
3. 前端弹出密码/验证码输入框
4. 用户输入密码后重新发起请求
5. 后端验证密码后执行操作
```

#### 接口设计

**请求二次验证Token**：
```
POST /api/auth/second-token
Request: { "password": "123456" }
Response: { "secondToken": "xxx", "expiresIn": 300 }
```

**携带Token执行敏感操作**：
```
DELETE /api/family/1
Header: X-Second-Auth: {secondToken}
```

### 2.3 XSS 防护

#### XSS 过滤工具

```java
public class XssUtil {
    
    private static final Pattern[] PATTERNS = {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE)
    };
    
    public static String stripXss(String value) {
        if (value == null) return null;
        
        for (Pattern pattern : PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }
        return value;
    }
}
```

#### XSS 过滤拦截器

```java
@Component
public class XssFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
    }
}

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    @Override
    public String getParameter(String name) {
        return XssUtil.stripXss(super.getParameter(name));
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        
        return Arrays.stream(values)
            .map(XssUtil::stripXss)
            .toArray(String[]::new);
    }
}
```

### 2.4 参数校验增强

#### 校验注解

```java
public class MemberRequest {
    @NotBlank(message = "姓名不能为空")
    @Length(max = 50, message = "姓名不能超过50个字符")
    private String name;
    
    @NotNull(message = "性别不能为空")
    @Pattern(regexp = "^(男|女|未知)$", message = "性别值不正确")
    private String gender;
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确")
    private String birthDate;
    
    @Length(max = 500, message = "简介不能超过500个字符")
    private String bio;
}
```

#### 全局校验处理

```java
@RestControllerAdvice
public class ValidationExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }
}
```

---

## 三、成员管理模块优化

### 3.1 成员列表分页查询增强

#### 接口设计

**接口路径**：`GET /api/admin/member`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认20 |
| name | String | 否 | 姓名（模糊匹配） |
| gender | String | 否 | 性别：男/女/未知 |
| birthDateStart | String | 否 | 出生日期开始 |
| birthDateEnd | String | 否 | 出生日期结束 |
| genealogyId | Long | 否 | 所属家谱ID |
| createTimeStart | String | 否 | 创建时间开始 |
| createTimeEnd | String | 否 | 创建时间结束 |

**请求示例**：
```
GET /api/admin/member?page=1&size=20&name=张&gender=男&genealogyId=1
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "records": [
      {
        "id": 1,
        "name": "张三",
        "gender": "男",
        "birthDate": "1990-01-01",
        "birthPlace": "北京市",
        "deathDate": null,
        "bio": "简介",
        "avatar": "https://xxx.com/avatar.jpg",
        "genealogyId": 1,
        "genealogyName": "张氏家谱",
        "userId": 2,
        "accountRole": "ADMIN",
        "createdAt": "2026-01-01 10:00:00"
      }
    ]
  }
}
```

### 3.2 后端实现

#### MemberQueryRequest

```java
@Data
public class MemberQueryRequest {
    private Integer page = 1;
    private Integer size = 20;
    private String name;
    private String gender;
    private String birthDateStart;
    private String birthDateEnd;
    private Long genealogyId;
    private String createTimeStart;
    private String createTimeEnd;
}
```

#### MemberMapper

```java
@Mapper
public interface MemberMapper extends BaseMapper<FamilyMember> {
    
    IPage<MemberVO> selectMemberPage(
        Page<?> page,
        @Param("query") MemberQueryRequest query
    );
}
```

#### MemberMapper.xml

```xml
<select id="selectMemberPage" resultType="com.kin.family.dto.MemberVO">
    SELECT 
        fm.id, fm.name, fm.gender, fm.birth_date, fm.birth_place, 
        fm.death_date, fm.bio, fm.avatar, fm.genealogy_id,
        f.name as genealogy_name,
        fm.user_id,
        ug.role as account_role,
        fm.created_at
    FROM family_member fm
    LEFT JOIN family f ON fm.genealogy_id = f.id
    LEFT JOIN user_genealogy ug ON fm.user_id = ug.user_id AND fm.genealogy_id = ug.genealogy_id
    <where>
        <if test="query.name != null and query.name != ''">
            AND fm.name LIKE CONCAT('%', #{query.name}, '%')
        </if>
        <if test="query.gender != null and query.gender != ''">
            AND fm.gender = #{query.gender}
        </if>
        <if test="query.birthDateStart != null and query.birthDateStart != ''">
            AND fm.birth_date >= #{query.birthDateStart}
        </if>
        <if test="query.birthDateEnd != null and query.birthDateEnd != ''">
            AND fm.birth_date <= #{query.birthDateEnd}
        </if>
        <if test="query.genealogyId != null">
            AND fm.genealogy_id = #{query.genealogyId}
        </if>
        <if test="query.createTimeStart != null and query.createTimeStart != ''">
            AND fm.created_at >= #{query.createTimeStart}
        </if>
        <if test="query.createTimeEnd != null and query.createTimeEnd != ''">
            AND fm.created_at <= #{query.createTimeEnd}
        </if>
    </where>
    ORDER BY fm.created_at DESC
</select>
```

### 3.3 成员编辑功能增强

#### 接口设计

**接口路径**：`PUT /api/admin/member/{id}`

**请求参数**：
```json
{
  "name": "张三",
  "gender": "男",
  "birthDate": "1990-01-01",
  "birthPlace": "北京市",
  "deathDate": null,
  "bio": "简介内容",
  "avatar": "https://xxx.com/avatar.jpg",
  "genealogyId": 1,
  "accountRole": "ADMIN"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

#### 跨家谱迁移确认接口

**接口路径**：`POST /api/admin/member/check-transfer`

**请求参数**：
```json
{
  "memberId": 1,
  "targetGenealogyId": 2
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "canTransfer": true,
    "warnings": [
      "将同时迁移该成员的关系数据（3条父子关系）",
      "目标家谱存在同名成员"
    ],
    "affectedRelations": 3
  }
}
```

---

## 四、前端实现

### 4.1 成员管理页面

```vue
<template>
  <div class="member-page">
    <!-- 搜索区域 -->
    <n-card class="search-card">
      <n-form :model="searchForm" inline>
        <n-form-item label="姓名">
          <n-input v-model:value="searchForm.name" placeholder="请输入姓名" clearable />
        </n-form-item>
        <n-form-item label="性别">
          <n-select v-model:value="searchForm.gender" :options="genderOptions" clearable />
        </n-form-item>
        <n-form-item label="出生日期">
          <n-date-picker v-model:value="searchForm.birthDateRange" type="daterange" />
        </n-form-item>
        <n-form-item label="所属家谱">
          <n-select v-model:value="searchForm.genealogyId" :options="genealogyOptions" clearable />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" @click="handleSearch">搜索</n-button>
          <n-button @click="handleReset">重置</n-button>
        </n-form-item>
      </n-form>
    </n-card>
    
    <!-- 表格区域 -->
    <n-card>
      <n-data-table :columns="columns" :data="tableData" :loading="loading" />
      <n-pagination v-model:page="pagination.page" :page-count="pagination.totalPages" 
                    @update:page="handlePageChange" />
    </n-card>
  </div>
</template>
```

### 4.2 搜索逻辑

```typescript
const searchForm = reactive({
  name: '',
  gender: null,
  birthDateRange: null,
  genealogyId: null,
  createTimeRange: null
})

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined,
      gender: searchForm.gender || undefined,
      birthDateStart: searchForm.birthDateRange?.[0],
      birthDateEnd: searchForm.birthDateRange?.[1],
      genealogyId: searchForm.genealogyId || undefined
    }
    const res = await getMemberList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}
```

---

## 四、小程序家谱树当前用户标识优化

### 4.1 需求说明

当前小程序端家谱树中，只在自己创建的家谱里标明用户身份，在申请加入的家谱中没有标明。这导致用户找不到自己在族谱中的位置。

### 4.2 优化目标

1. 在家谱树中清晰标识当前用户身份
2. 无论是创建的家谱还是加入的家谱，都能看到"我"的位置

### 4.3 标识规则

| 用户角色 | 显示标识 | 说明 |
|----------|----------|------|
| 创建者 | 👑 创建者 | 家谱的创建者 |
| 管理员 | 📛 管理员 | 家谱的管理员 |
| 普通成员 | 👤 我 | 家谱的普通成员 |
| 未登录/非成员 | 无标识 | 不显示 |

### 4.4 后端实现

#### TreeNodeVO 扩展

```java
@Data
@Builder
public class TreeNodeVO {
    private Long id;
    private String name;
    private String gender;
    private String avatar;
    private Long userId;
    private Boolean currentUser;
    private String currentUserLabel;
    // ... 其他字段
}
```

#### FamilyTreeService 改造

```java
public List<TreeNodeVO> getFamilyTree(Long familyId, Long currentUserId) {
    // 1. 获取家谱树结构
    List<TreeNodeVO> tree = buildFamilyTree(familyId);
    
    // 2. 获取当前用户在该家谱中的角色
    UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(currentUserId, familyId);
    
    // 3. 标记当前用户节点
    if (ug != null) {
        String roleLabel = getRoleLabel(ug.getRole(), familyId, currentUserId);
        markCurrentUserNode(tree, currentUserId, roleLabel);
    }
    
    return tree;
}

private String getRoleLabel(String role, Long familyId, Long userId) {
    Family family = familyMapper.selectById(familyId);
    if (family.getCreatedBy().equals(userId)) {
        return "创建者";
    }
    if ("ADMIN".equals(role)) {
        return "管理员";
    }
    return "我";
}

private void markCurrentUserNode(List<TreeNodeVO> tree, Long userId, String roleLabel) {
    for (TreeNodeVO node : tree) {
        if (userId.equals(node.getUserId())) {
            node.setCurrentUser(true);
            node.setCurrentUserLabel(roleLabel);
            break;
        }
    }
}
```

### 4.5 前端实现

```vue
<template>
  <view class="tree-node" :class="{ 'is-current-user': node.currentUser }">
    <image :src="node.avatar || defaultAvatar" class="avatar" />
    <view class="info">
      <view class="name-row">
        <text class="name">{{ node.name }}</text>
        <text v-if="node.currentUser" class="user-badge">
          {{ node.currentUserLabel }}
        </text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.tree-node {
  &.is-current-user {
    background: #e8f5e9;
    border: 2rpx solid #4caf50;
  }
  
  .user-badge {
    margin-left: 12rpx;
    padding: 4rpx 12rpx;
    font-size: 24rpx;
    color: #4caf50;
    background: #c8e6c9;
    border-radius: 8rpx;
  }
}
</style>
```

### 4.6 接口响应示例

```json
{
  "id": 1,
  "name": "张三",
  "userId": 100,
  "currentUser": true,
  "currentUserLabel": "我",
  "children": []
}
```

---

## 五、测试用例

### 5.1 接口安全测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 登录限流 | 连续6次错误登录 | 提示请求过于频繁 |
| XSS攻击 | 提交`<script>alert(1)</script>` | 脚本被过滤 |
| 参数校验 | 姓名超长 | 提示姓名不能超过50个字符 |

### 5.2 成员管理测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 姓名模糊搜索 | 搜索"张" | 返回所有姓张的成员 |
| 组合条件搜索 | 姓名=张三 AND 家谱=张氏 | 返回张氏家谱中叫张三的成员 |
| 跨家谱迁移 | 修改成员所属家谱 | 弹出确认框，提示影响范围 |

### 5.3 家谱树标识测试

| 测试项 | 场景 | 预期结果 |
|--------|------|----------|
| 创建者标识 | 用户是家谱创建者 | 显示"👑 创建者" |
| 管理员标识 | 用户是家谱管理员 | 显示"📛 管理员" |
| 普通成员标识 | 用户是普通成员 | 显示"👤 我" |
| 非成员 | 用户不是该家谱成员 | 不显示标识 |

---

## 六、交付物

- [ ] @RateLimit 限流注解和切面
- [ ] @SecondAuth 二次验证功能
- [ ] XSS 过滤拦截器
- [ ] 参数校验增强
- [ ] MemberQueryRequest 查询参数类
- [ ] MemberMapper.xml 分页查询
- [ ] 成员列表页面改造
- [ ] 跨家谱迁移功能
- [ ] TreeNodeVO 扩展字段
- [ ] 家谱树用户标识逻辑
- [ ] 前端节点渲染改造
- [ ] 单元测试代码
