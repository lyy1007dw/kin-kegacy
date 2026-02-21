# Day 1 - JWT 认证基础架构搭建

**开发日期**：第 1 天
**优先级**：P0（高）
**所属阶段**：安全加固

---

## 一、功能需求描述

### 1.1 背景

当前系统使用简单的 `X-User-Id` Header 进行用户识别，存在严重安全隐患。需要实现完整的 JWT（JSON Web Token）认证机制，保障系统安全性。

### 1.2 功能目标

- 引入 JWT 依赖，搭建认证基础架构
- 实现 Token 生成、解析、验证功能
- 创建认证拦截器和注解
- 改造现有登录接口，返回 JWT Token

### 1.3 技术方案

采用 JJwt 库实现 JWT 功能：

```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: { "userId": 1, "username": "admin", "role": "SUPER_ADMIN", "exp": 1700000000 }
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

---

## 二、数据库设计

### 2.1 新增表结构

无需新增表，JWT 为无状态认证。

### 2.2 配置项

在 `application.yml` 中新增 JWT 配置：

```yaml
jwt:
  secret: kin-legacy-jwt-secret-key-2026
  expiration: 86400000  # 24小时（毫秒）
  refresh-expiration: 604800000  # 7天（毫秒）
  header: Authorization
  prefix: "Bearer "
```

---

## 三、后端接口设计

### 3.1 依赖引入

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 3.2 核心类设计

#### 3.2.1 JwtUtil 工具类

**路径**：`com.kin.family.util.JwtUtil`

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| generateToken | Long userId, String username, String role | String | 生成访问 Token |
| generateRefreshToken | Long userId | String | 生成刷新 Token |
| parseToken | String token | Claims | 解析 Token 获取声明 |
| getUserIdFromToken | String token | Long | 从 Token 获取用户 ID |
| validateToken | String token | boolean | 验证 Token 有效性 |
| isTokenExpired | String token | boolean | 判断 Token 是否过期 |

#### 3.2.2 JwtAuthenticationInterceptor 拦截器

**路径**：`com.kin.family.interceptor.JwtAuthenticationInterceptor`

**功能**：
- 从请求头获取 Token
- 验证 Token 有效性
- 解析用户信息并存入 ThreadLocal
- 放行合法请求

#### 3.2.3 注解定义

**路径**：`com.kin.family.annotation.*`

| 注解 | 说明 |
|------|------|
| `@RequireLogin` | 标记需要登录的接口 |
| `@RequireRole` | 标记需要特定角色的接口 |

### 3.3 接口改造

#### 登录接口改造

**接口路径**：`POST /api/auth/login`

**请求参数**：
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "role": "SUPER_ADMIN"
    }
  }
}
```

#### Token 刷新接口（新增）

**接口路径**：`POST /api/auth/refresh`

**请求参数**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

#### 登出接口改造

**接口路径**：`POST /api/auth/logout`

**请求头**：`Authorization: Bearer {token}`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 四、前端改造

### 4.1 管理后台改造

#### request.ts 改造

```typescript
// 请求拦截器添加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器处理 Token 过期
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token 过期，尝试刷新或跳转登录
    }
    return Promise.reject(error)
  }
)
```

### 4.2 小程序改造

#### request.js 改造

```javascript
// 请求拦截
const header = {
  'Content-Type': 'application/json'
}
const token = uni.getStorageSync('accessToken')
if (token) {
  header.Authorization = `Bearer ${token}`
}
```

---

## 五、测试用例

### 5.1 单元测试

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| Token 生成 | 调用 generateToken | 返回有效 JWT 字符串 |
| Token 解析 | 解析生成的 Token | 正确获取用户信息 |
| Token 验证 | 验证有效 Token | 返回 true |
| Token 过期 | 验证过期 Token | 返回 false |
| Token 篡改 | 验证被篡改的 Token | 返回 false |

### 5.2 接口测试

| 测试项 | 请求 | 预期结果 |
|--------|------|----------|
| 正常登录 | 正确用户名密码 | 返回 Token 和用户信息 |
| 错误密码 | 错误密码 | 返回 401 错误 |
| 无 Token 访问 | 不带 Token 访问受保护接口 | 返回 401 错误 |
| 有效 Token 访问 | 带有效 Token 访问 | 正常返回数据 |
| Token 刷新 | 使用 refreshToken | 返回新 Token |

---

## 六、交付物

- [ ] JwtUtil 工具类
- [ ] JwtAuthenticationInterceptor 拦截器
- [ ] @RequireLogin、@RequireRole 注解
- [ ] WebMvcConfig 配置更新
- [ ] AuthController 接口改造
- [ ] 前端请求拦截器改造
- [ ] 单元测试代码

---

## 七、风险点

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| Token 泄露 | 账号被盗用 | 设置较短过期时间，支持手动失效 |
| 并发刷新 | 多次刷新产生多个 Token | 使用 Redis 存储 Token 版本 |
| 秘钥安全 | 秘钥泄露可伪造 Token | 秘钥配置在环境变量中 |
