# Day 3 - 密码加密与用户表改造

**开发日期**：第 3 天
**优先级**：P0（高）
**所属阶段**：安全加固

---

## 一、功能需求描述

### 1.1 背景

当前系统密码以明文形式存储，存在严重安全隐患。需要实现密码加密存储，同时完成用户角色体系的数据模型改造。

### 1.2 功能目标

- 实现 BCrypt 密码加密
- 改造用户表，新增角色相关字段
- 创建用户-家谱关联表
- 实现角色自动升级/降级逻辑
- 数据迁移：历史密码加密处理

### 1.3 技术方案

使用 Spring Security 的 BCryptPasswordEncoder：

- 加密强度：10（默认）
- 每次加密生成不同密文（含随机盐）
- 验证时对比明文与密文

---

## 二、数据库设计

### 2.1 用户表改造

在现有 `user` 表基础上新增字段：

```sql
ALTER TABLE user ADD COLUMN global_role VARCHAR(20) DEFAULT 'NORMAL_USER' COMMENT '全局角色: SUPER_ADMIN/GENEALOGY_ADMIN/NORMAL_USER';
ALTER TABLE user ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间';
ALTER TABLE user ADD COLUMN login_fail_count INT DEFAULT 0 COMMENT '登录失败次数';
ALTER TABLE user ADD COLUMN lock_time DATETIME COMMENT '锁定时间';
ALTER TABLE user ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用';
```

完整用户表结构：

```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE COMMENT '用户名',
  password VARCHAR(100) COMMENT '密码(BCrypt加密)',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar VARCHAR(500) COMMENT '头像URL',
  phone VARCHAR(20) COMMENT '手机号',
  openid VARCHAR(100) UNIQUE COMMENT '微信OpenID',
  global_role VARCHAR(20) DEFAULT 'NORMAL_USER' COMMENT '全局角色',
  last_login_time DATETIME COMMENT '最后登录时间',
  login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
  lock_time DATETIME COMMENT '锁定时间',
  status TINYINT DEFAULT 1 COMMENT '状态',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_openid (openid),
  INDEX idx_global_role (global_role)
) COMMENT '用户表';
```

### 2.2 用户-家谱关联表（新增）

```sql
CREATE TABLE user_genealogy (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  genealogy_id BIGINT NOT NULL COMMENT '家谱ID',
  role VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色: ADMIN/MEMBER',
  family_member_id BIGINT COMMENT '关联的家族成员ID',
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  created_by BIGINT COMMENT '操作人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_genealogy (user_id, genealogy_id),
  INDEX idx_user_id (user_id),
  INDEX idx_genealogy_id (genealogy_id),
  INDEX idx_role (role)
) COMMENT '用户-家谱关联表';
```

### 2.3 数据迁移

```sql
-- 迁移现有管理员账号
UPDATE user SET global_role = 'SUPER_ADMIN' WHERE id = 1;

-- 迁移现有家谱创建者
INSERT INTO user_genealogy (user_id, genealogy_id, role, joined_at)
SELECT created_by, id, 'ADMIN', created_at
FROM family
WHERE created_by IS NOT NULL;

-- 密码加密迁移（Java代码处理）
-- 在启动时检测明文密码并加密
```

---

## 三、后端接口设计

### 3.1 密码加密服务

#### PasswordService

**路径**：`com.kin.family.service.PasswordService`

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| encode | String rawPassword | String | 加密密码 |
| matches | String rawPassword, String encodedPassword | boolean | 验证密码 |
| upgradeEncoding | String encodedPassword | boolean | 是否需要升级加密 |

```java
@Service
public class PasswordService {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    public boolean upgradeEncoding(String encodedPassword) {
        return encoder.upgradeEncoding(encodedPassword);
    }
}
```

### 3.2 角色管理服务

#### UserRoleService

**路径**：`com.kin.family.service.UserRoleService`

| 方法 | 说明 |
|------|------|
| updateUserRole(Long userId, Long genealogyId, String role) | 更新用户在家谱中的角色 |
| checkAndUpgradeGlobalRole(Long userId) | 检查并升级全局角色 |
| checkAndDowngradeGlobalRole(Long userId) | 检查并降级全局角色 |
| isAdminOfGenealogy(Long userId, Long genealogyId) | 判断是否为家谱管理员 |
| isOnlyAdminOfGenealogy(Long userId, Long genealogyId) | 判断是否为唯一管理员 |
| getAdminCount(Long genealogyId) | 获取家谱管理员数量 |
| getUserGenealogies(Long userId) | 获取用户所属家谱列表 |

### 3.3 接口设计

#### 修改用户角色（超级管理员）

**接口路径**：`PUT /api/admin/user/{userId}/role`

**请求参数**：
```json
{
  "genealogyId": 1,
  "role": "ADMIN"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误场景**：
```json
{
  "code": 400,
  "message": "该用户是「张氏家谱」的唯一管理员，请先指定其他管理员",
  "data": null
}
```

#### 获取用户所属家谱

**接口路径**：`GET /api/user/{userId}/genealogies`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "genealogyId": 1,
      "genealogyName": "张氏家谱",
      "role": "ADMIN",
      "joinedAt": "2026-01-01 10:00:00"
    },
    {
      "genealogyId": 2,
      "genealogyName": "李氏家谱",
      "role": "MEMBER",
      "joinedAt": "2026-02-01 10:00:00"
    }
  ]
}
```

#### 批量降级用户角色

**接口路径**：`PUT /api/admin/user/{userId}/downgrade`

**请求参数**：
```json
{
  "genealogyIds": [1, 2, 3]
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": [1, 2],
    "failed": [
      {
        "genealogyId": 3,
        "reason": "该用户是唯一管理员"
      }
    ]
  }
}
```

---

## 四、角色自动升级/降级逻辑

### 4.1 升级逻辑

```
触发条件：用户被设置为某家谱管理员
检查逻辑：
  1. 查询用户在所有家谱中的角色
  2. 如果存在任意 ADMIN 角色
  3. 且当前全局角色为 NORMAL_USER
  4. 则升级为 GENEALOGY_ADMIN
```

### 4.2 降级逻辑

```
触发条件：用户被取消家谱管理员身份
检查逻辑：
  1. 查询用户在所有家谱中的角色
  2. 如果不存在任何 ADMIN 角色
  3. 且当前全局角色为 GENEALOGY_ADMIN
  4. 则降级为 NORMAL_USER
```

### 4.3 唯一管理员保护

```
触发场景：取消用户管理员身份前
检查逻辑：
  1. 查询该家谱的管理员数量
  2. 如果只有1个管理员
  3. 且该管理员就是目标用户
  4. 则拒绝操作，返回错误提示
```

---

## 五、后端实现

### 5.1 实体类

#### UserGenealogy 实体

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_genealogy")
public class UserGenealogy {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long genealogyId;
    private String role;  // ADMIN, MEMBER
    private Long familyMemberId;
    private LocalDateTime joinedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 5.2 Mapper 接口

```java
@Mapper
public interface UserGenealogyMapper extends BaseMapper<UserGenealogy> {
    
    @Select("SELECT ug.*, f.name as genealogyName FROM user_genealogy ug " +
            "LEFT JOIN family f ON ug.genealogy_id = f.id " +
            "WHERE ug.user_id = #{userId}")
    List<UserGenealogyVO> selectByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM user_genealogy " +
            "WHERE genealogy_id = #{genealogyId} AND role = 'ADMIN'")
    int countAdmins(Long genealogyId);
    
    @Select("SELECT * FROM user_genealogy " +
            "WHERE user_id = #{userId} AND genealogy_id = #{genealogyId}")
    UserGenealogy selectByUserAndGenealogy(Long userId, Long genealogyId);
}
```

### 5.3 Service 实现

```java
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
    
    private final UserGenealogyMapper userGenealogyMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public void updateUserRole(Long userId, Long genealogyId, String role) {
        // 检查是否为唯一管理员
        if ("MEMBER".equals(role) && isOnlyAdminOfGenealogy(userId, genealogyId)) {
            throw new BusinessException("该用户是该家谱的唯一管理员，无法降级");
        }
        
        // 更新角色
        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        if (ug != null) {
            ug.setRole(role);
            userGenealogyMapper.updateById(ug);
        } else {
            ug = UserGenealogy.builder()
                .userId(userId)
                .genealogyId(genealogyId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
            userGenealogyMapper.insert(ug);
        }
        
        // 更新全局角色
        if ("ADMIN".equals(role)) {
            checkAndUpgradeGlobalRole(userId);
        } else {
            checkAndDowngradeGlobalRole(userId);
        }
    }
    
    @Override
    public void checkAndUpgradeGlobalRole(Long userId) {
        User user = userMapper.selectById(userId);
        if ("NORMAL_USER".equals(user.getGlobalRole())) {
            // 检查是否拥有管理员身份
            int adminCount = userGenealogyMapper.selectCount(
                new LambdaQueryWrapper<UserGenealogy>()
                    .eq(UserGenealogy::getUserId, userId)
                    .eq(UserGenealogy::getRole, "ADMIN")
            );
            if (adminCount > 0) {
                user.setGlobalRole("GENEALOGY_ADMIN");
                userMapper.updateById(user);
            }
        }
    }
    
    @Override
    public void checkAndDowngradeGlobalRole(Long userId) {
        User user = userMapper.selectById(userId);
        if ("GENEALOGY_ADMIN".equals(user.getGlobalRole())) {
            int adminCount = userGenealogyMapper.selectCount(
                new LambdaQueryWrapper<UserGenealogy>()
                    .eq(UserGenealogy::getUserId, userId)
                    .eq(UserGenealogy::getRole, "ADMIN")
            );
            if (adminCount == 0) {
                user.setGlobalRole("NORMAL_USER");
                userMapper.updateById(user);
            }
        }
    }
    
    @Override
    public boolean isOnlyAdminOfGenealogy(Long userId, Long genealogyId) {
        int adminCount = userGenealogyMapper.countAdmins(genealogyId);
        if (adminCount != 1) return false;
        
        UserGenealogy ug = userGenealogyMapper.selectByUserAndGenealogy(userId, genealogyId);
        return ug != null && "ADMIN".equals(ug.getRole());
    }
}
```

---

## 六、数据迁移脚本

### 6.1 密码加密迁移

```java
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {
    
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    
    @Override
    public void run(String... args) {
        // 查询所有用户
        List<User> users = userMapper.selectList(null);
        
        for (User user : users) {
            String password = user.getPassword();
            // 判断是否为明文密码（BCrypt密文以$2a$开头）
            if (password != null && !password.startsWith("$2a$")) {
                // 加密密码
                String encoded = passwordService.encode(password);
                user.setPassword(encoded);
                userMapper.updateById(user);
                log.info("已加密用户 {} 的密码", user.getUsername());
            }
        }
    }
}
```

### 6.2 角色数据迁移

```sql
-- 初始化超级管理员
UPDATE user SET global_role = 'SUPER_ADMIN' WHERE username = 'admin';

-- 为现有家谱创建者建立关联
INSERT INTO user_genealogy (user_id, genealogy_id, role, joined_at, created_by)
SELECT created_by, id, 'ADMIN', created_at, created_by
FROM family
WHERE created_by IS NOT NULL
ON DUPLICATE KEY UPDATE role = 'ADMIN';

-- 更新全局角色
UPDATE user u
SET global_role = 'GENEALOGY_ADMIN'
WHERE EXISTS (
  SELECT 1 FROM user_genealogy ug
  WHERE ug.user_id = u.id AND ug.role = 'ADMIN'
) AND u.global_role = 'NORMAL_USER';
```

---

## 七、测试用例

### 7.1 密码加密测试

| 测试项 | 输入 | 预期结果 |
|--------|------|----------|
| 密码加密 | 123456 | 生成BCrypt密文 |
| 密码验证 | 123456, 密文 | 返回true |
| 错误密码 | wrong, 密文 | 返回false |
| 相同密码 | 123456加密两次 | 生成不同密文 |

### 7.2 角色升级测试

| 测试项 | 初始状态 | 操作 | 预期结果 |
|--------|----------|------|----------|
| 升级为管理员 | NORMAL_USER | 设置为某家谱管理员 | GENEALOGY_ADMIN |
| 降级为普通用户 | GENEALOGY_ADMIN | 取消所有家谱管理员 | NORMAL_USER |
| 唯一管理员保护 | 唯一管理员 | 尝试降级 | 操作失败 |

---

## 八、交付物

- [ ] PasswordService 密码加密服务
- [ ] UserGenealogy 实体类和 Mapper
- [ ] UserRoleService 角色管理服务
- [ ] 用户表改造SQL
- [ ] 数据迁移脚本
- [ ] 登录接口密码验证改造
- [ ] 单元测试代码
