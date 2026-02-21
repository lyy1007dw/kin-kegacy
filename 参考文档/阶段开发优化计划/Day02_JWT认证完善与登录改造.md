# Day 2 - JWT 认证完善与登录改造

**开发日期**：第 2 天
**优先级**：P0（高）
**所属阶段**：安全加固

---

## 一、功能需求描述

### 1.1 背景

完成 JWT 基础架构后，需要完善认证流程，包括微信小程序登录改造、Token 刷新机制、登录状态管理等功能。

### 1.2 功能目标

- 改造微信小程序登录接口，支持 JWT
- 实现 Token 自动刷新机制
- 添加登录失败次数限制
- 实现登录日志记录
- 完善前端登录流程

### 1.3 登录流程设计

#### 管理后台登录流程

```
用户输入账号密码 → 后端验证 → 生成JWT Token → 返回Token和用户信息
前端存储Token → 后续请求携带Token → 后端验证Token → 放行请求
```

#### 小程序微信登录流程

```
wx.login() 获取code → 发送code到后端 → 后端调用微信API获取openid
→ 查询/创建用户 → 生成JWT Token → 返回Token和用户信息
```

---

## 二、后端接口设计

### 2.1 管理后台登录接口

**接口路径**：`POST /api/auth/login`

**请求参数**：
```json
{
  "username": "admin",
  "password": "123456",
  "captcha": "abc123",
  "captchaKey": "uuid-xxx"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": "https://xxx.com/avatar.jpg",
      "role": "SUPER_ADMIN",
      "globalRole": "SUPER_ADMIN"
    }
  }
}
```

### 2.2 小程序微信登录接口

**接口路径**：`POST /api/auth/wx-login`

**请求参数**：
```json
{
  "code": "081xxx",
  "nickname": "用户昵称",
  "avatar": "头像URL"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 2,
      "openid": "oXXXX",
      "nickname": "用户昵称",
      "avatar": "头像URL",
      "role": "NORMAL_USER",
      "globalRole": "NORMAL_USER"
    },
    "isNewUser": true
  }
}
```

### 2.3 Token 刷新接口

**接口路径**：`POST /api/auth/refresh`

**请求参数**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

**错误响应**：
```json
{
  "code": 401,
  "message": "Refresh token已过期，请重新登录",
  "data": null
}
```

### 2.4 获取当前用户信息

**接口路径**：`GET /api/auth/user-info`

**请求头**：`Authorization: Bearer {token}`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "https://xxx.com/avatar.jpg",
    "role": "SUPER_ADMIN",
    "globalRole": "SUPER_ADMIN",
    "families": [
      {
        "familyId": 1,
        "familyName": "张氏家谱",
        "role": "ADMIN"
      }
    ]
  }
}
```

### 2.5 登出接口

**接口路径**：`POST /api/auth/logout`

**请求头**：`Authorization: Bearer {token}`

**响应结构**：
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

## 三、登录失败次数限制

### 3.1 数据结构

使用 Redis 存储登录失败次数：

```
Key: login:fail:{username}
Value: 失败次数
TTL: 30分钟
```

### 3.2 限制规则

| 失败次数 | 限制措施 |
|----------|----------|
| 1-3 次 | 无限制，提示密码错误 |
| 4-5 次 | 需要输入验证码 |
| 6次以上 | 账号锁定30分钟 |

### 3.3 接口响应

**账号锁定响应**：
```json
{
  "code": 423,
  "message": "账号已被锁定，请30分钟后再试",
  "data": {
    "lockExpireTime": "2026-02-21 15:30:00"
  }
}
```

---

## 四、登录日志记录

### 4.1 数据库设计

新增登录日志表 `login_log`：

```sql
CREATE TABLE login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINt COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  login_type VARCHAR(20) COMMENT '登录类型: PASSWORD/WECHAT',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  location VARCHAR(100) COMMENT '登录地点',
  device VARCHAR(200) COMMENT '设备信息',
  browser VARCHAR(100) COMMENT '浏览器',
  os VARCHAR(100) COMMENT '操作系统',
  status TINYINT COMMENT '状态: 0失败 1成功',
  message VARCHAR(500) COMMENT '登录消息',
  login_time DATETIME COMMENT '登录时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_login_time (login_time)
) COMMENT '登录日志表';
```

### 4.2 记录内容

| 字段 | 获取方式 |
|------|----------|
| ip_address | request.getRemoteAddr() |
| location | 调用IP定位API |
| device | User-Agent解析 |
| browser | User-Agent解析 |
| os | User-Agent解析 |

---

## 五、前端改造

### 5.1 管理后台

#### 登录页面改造

```vue
<template>
  <div class="login-container">
    <n-form ref="formRef" :model="form" :rules="rules">
      <n-form-item label="用户名" path="username">
        <n-input v-model:value="form.username" placeholder="请输入用户名" />
      </n-form-item>
      <n-form-item label="密码" path="password">
        <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
      </n-form-item>
      <!-- 验证码（失败3次后显示） -->
      <n-form-item v-if="showCaptcha" label="验证码" path="captcha">
        <n-input v-model:value="form.captcha" placeholder="请输入验证码" />
        <img :src="captchaUrl" @click="refreshCaptcha" />
      </n-form-item>
      <n-button type="primary" @click="handleLogin" :loading="loading">登录</n-button>
    </n-form>
  </div>
</template>
```

#### Token 管理工具

```typescript
// utils/auth.ts
const TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}
```

#### Token 自动刷新

```typescript
// utils/request.ts
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

async function refreshToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    clearToken()
    router.push('/login')
    return null
  }
  
  try {
    const res = await axios.post('/api/auth/refresh', { refreshToken })
    const { accessToken, refreshToken: newRefreshToken } = res.data.data
    setToken(accessToken)
    setRefreshToken(newRefreshToken)
    return accessToken
  } catch (error) {
    clearToken()
    router.push('/login')
    return null
  }
}

// 响应拦截器
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      if (!isRefreshing) {
        isRefreshing = true
        const newToken = await refreshToken()
        isRefreshing = false
        
        if (newToken) {
          error.config.headers.Authorization = `Bearer ${newToken}`
          return axios(error.config)
        }
      }
    }
    return Promise.reject(error)
  }
)
```

### 5.2 小程序

#### 登录流程

```javascript
// pages/login/login.vue
async handleWxLogin() {
  // 获取微信登录code
  const { code } = await uni.login({ provider: 'weixin' })
  
  // 获取用户信息
  const { userInfo } = await uni.getUserProfile({ desc: '用于完善用户资料' })
  
  // 调用后端登录接口
  const res = await request({
    url: '/api/auth/wx-login',
    method: 'POST',
    data: {
      code,
      nickname: userInfo.nickName,
      avatar: userInfo.avatarUrl
    }
  })
  
  // 存储Token
  uni.setStorageSync('accessToken', res.data.accessToken)
  uni.setStorageSync('refreshToken', res.data.refreshToken)
  uni.setStorageSync('userInfo', res.data.userInfo)
  
  // 更新Store
  this.$store.commit('setToken', res.data.accessToken)
  this.$store.commit('setUserInfo', res.data.userInfo)
  
  // 跳转首页
  uni.switchTab({ url: '/pages/index/index' })
}
```

---

## 六、测试用例

### 6.1 登录测试

| 测试项 | 输入 | 预期结果 |
|--------|------|----------|
| 正确登录 | admin/123456 | 返回Token，跳转首页 |
| 错误密码 | admin/wrong | 提示密码错误 |
| 用户不存在 | unknown/123456 | 提示用户不存在 |
| 连续失败5次 | 连续5次错误密码 | 要求输入验证码 |
| 连续失败6次 | 连续6次错误密码 | 账号锁定30分钟 |
| Token过期 | 过期Token访问 | 自动刷新或跳转登录 |

### 6.2 小程序登录测试

| 测试项 | 场景 | 预期结果 |
|--------|------|----------|
| 新用户登录 | 首次微信登录 | 创建新用户，返回Token |
| 老用户登录 | 已有账号 | 返回Token和用户信息 |
| Token刷新 | Token过期 | 使用refreshToken获取新Token |

---

## 七、交付物

- [ ] AuthController 接口完善
- [ ] LoginLog 实体类和 Mapper
- [ ] 登录失败次数限制功能
- [ ] 登录日志记录功能
- [ ] 前端登录页面改造
- [ ] Token 管理工具
- [ ] Token 自动刷新机制
- [ ] 小程序登录流程改造

---

## 八、注意事项

1. 微信小程序登录需要配置 AppID 和 AppSecret
2. Token 存储建议使用 HttpOnly Cookie（可选）
3. 生产环境需要配置 HTTPS
4. 敏感操作需要二次验证
