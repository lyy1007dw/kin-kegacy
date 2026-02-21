# 家谱小程序 - API接口文档

## 接口概览

本文档列出了小程序所使用的后端API接口及其实现状态。

---

## 1. 认证模块 `/api/auth`

| 接口 | 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|------|
| 微信登录 | POST | `/api/auth/wx-login` | ✅ 已实现 | 通过微信code获取token |
| 用户名登录 | POST | `/api/auth/login` | ✅ 已实现 | 用户名密码登录 |
| 获取当前用户 | GET | `/api/auth/me` | ✅ 已实现 | 获取当前登录用户信息 |
| 退出登录 | POST | `/api/auth/logout` | ✅ 已实现 | 清除登录状态 |

### 请求/响应格式

**WxLoginRequest:**
```json
{
  "code": "微信登录code"
}
```

**LoginRequest:**
```json
{
  "username": "用户名",
  "password": "密码"
}
```

**LoginResponse:**
```json
{
  "token": "JWT令牌",
  "user": {
    "id": 1,
    "nickname": "用户昵称",
    "avatar": "头像URL",
    "phone": "手机号",
    "role": "user"
  }
}
```

---

## 2. 家谱模块 `/api/family`

| 接口 | 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|------|
| 创建家谱 | POST | `/api/family/create` | ✅ 已实现 | 创建新的家谱 |
| 加入家谱 | POST | `/api/family/join` | ✅ 已实现 | 通过家谱码申请加入 |
| 获取家谱详情 | GET | `/api/family/{id}` | ✅ 已实现 | 根据ID获取家谱信息 |
| 获取我的家谱 | GET | `/api/family/mine` | ✅ 已实现 | 获取用户创建和加入的家谱列表 |
| 根据家谱码查询 | GET | `/api/family/code/{code}` | ✅ 已实现 | 根据家谱码获取家谱信息 |
| 获取所有家谱 | GET | `/api/family/list` | ✅ 已实现 | 获取所有家谱列表 |
| 分页获取家谱 | GET | `/api/family/list/paged` | ✅ 已实现 | 分页获取家谱列表 |
| 更新家谱 | PUT | `/api/family/{id}` | ✅ 已实现 | 更新家谱信息 |
| 删除家谱 | DELETE | `/api/family/{id}` | ✅ 已实现 | 删除家谱 |

### 请求/响应格式

**CreateFamilyRequest:**
```json
{
  "name": "家谱名称",
  "description": "简介",
  "avatar": "头像URL"
}
```

**JoinFamilyRequest:**
```json
{
  "code": "6位家谱码",
  "name": "申请人姓名",
  "relationDesc": "与成员的关系描述"
}
```

**FamilyDetailResponse:**
```json
{
  "id": 1,
  "name": "张氏家族",
  "code": "882910",
  "avatar": "头像URL",
  "description": "家族简介",
  "creatorId": 1,
  "memberCount": 5,
  "createTime": "2024-01-01T00:00:00"
}
```

---

## 3. 成员模块 `/api/family/{familyId}`

| 接口 | 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|------|
| 获取成员列表 | GET | `/api/family/{familyId}/members` | ✅ 已实现 | 获取家谱所有成员 |
| 获取家谱树 | GET | `/api/family/{familyId}/tree` | ✅ 已实现 | 获取树形结构的家谱数据 |
| 获取成员详情 | GET | `/api/family/{familyId}/member/{id}` | ✅ 已实现 | 获取单个成员详情 |
| 添加成员 | POST | `/api/family/{familyId}/member` | ✅ 已实现 | 添加新成员 |
| 申请修改信息 | POST | `/api/family/{familyId}/member/{id}/edit-request` | ✅ 已实现 | 成员申请修改自己的信息 |
| 更新成员 | PUT | `/api/family/{familyId}/member/{id}` | ✅ 已实现 | 直接更新成员信息(创建者权限) |
| 删除成员 | DELETE | `/api/family/{familyId}/member/{id}` | ✅ 已实现 | 删除成员(创建者权限) |

### 请求/响应格式

**AddMemberRequest:**
```json
{
  "name": "成员姓名",
  "gender": "male",
  "avatar": "头像URL",
  "birthDate": "1990-01-01",
  "bio": "个人简介",
  "parentId": 1
}
```

**EditMemberRequest:**
```json
{
  "fieldName": "bio",
  "oldValue": "旧值",
  "newValue": "新值"
}
```

**MemberResponse:**
```json
{
  "id": 1,
  "familyId": 1,
  "familyName": "张氏家族",
  "userId": 1,
  "name": "张三",
  "gender": "male",
  "avatar": "头像URL",
  "birthDate": "1990-01-01",
  "bio": "个人简介",
  "isCreator": 1,
  "children": [],
  "createTime": "2024-01-01T00:00:00"
}
```

**TreeNodeVO:**
```json
{
  "id": 1,
  "name": "张三",
  "gender": "male",
  "avatar": "头像URL",
  "birthDate": "1990-01-01",
  "bio": "个人简介",
  "isCreator": 1,
  "spouse": null,
  "children": [
    {
      "id": 2,
      "name": "张大",
      "gender": "male",
      "avatar": "头像URL",
      "birthDate": "2015-01-01",
      "bio": "个人简介",
      "isCreator": 0,
      "spouse": null,
      "children": []
    }
  ]
}
```

---

## 4. 审批模块 `/api/family/{familyId}`

| 接口 | 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|------|
| 获取审批列表 | GET | `/api/family/{familyId}/approvals` | ✅ 已实现 | 获取家谱的审批列表(分页) |
| 获取所有审批 | GET | `/api/approvals/all` | ✅ 已实现 | 获取用户可见的所有审批(分页) |
| 处理审批 | POST | `/api/family/{familyId}/approval/{requestId}/handle` | ✅ 已实现 | 同意或拒绝审批 |

### 请求/响应格式

**HandleApprovalRequest:**
```json
{
  "action": "approve",
  "remark": "备注"
}
```

**ApprovalResponse:**
```json
{
  "id": 1,
  "type": "join",
  "familyId": 1,
  "applicantUserId": 2,
  "applicantName": "李四",
  "relationDesc": "张三的儿子",
  "memberId": null,
  "fieldName": null,
  "oldValue": null,
  "newValue": null,
  "status": "pending",
  "createTime": "2024-01-01T00:00:00"
}
```

---

## 5. 用户模块 `/api/user`

| 接口 | 方法 | 路径 | 状态 | 说明 |
|------|------|------|------|------|
| 获取当前用户 | GET | `/api/user/me` | ✅ 已实现 | 获取当前登录用户信息 |
| 更新用户 | PUT | `/api/user/{id}` | ✅ 已实现 | 更新用户信息 |
| 删除用户 | DELETE | `/api/user/{id}` | ✅ 已实现 | 禁用用户 |

---

## 通用响应格式

**成功响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1704067200000
}
```

**错误响应:**
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null,
  "timestamp": 1704067200000
}
```

**分页响应:**
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "size": 10
}
```

---

## 枚举值说明

**Gender (性别):**
- `male` - 男
- `female` - 女

**RequestStatus (审批状态):**
- `pending` - 待审批
- `approved` - 已同意
- `rejected` - 已拒绝

**UserRole (用户角色):**
- `admin` - 管理员
- `user` - 普通用户

---

## 待实现/建议补充的接口

### 1. 文件上传
```
POST /api/upload/avatar - 上传头像
POST /api/upload/image - 上传图片
```

### 2. 消息通知
```
GET /api/notifications - 获取消息列表
POST /api/notifications/read - 标记已读
```

### 3. 搜索功能
```
GET /api/family/search - 搜索家谱
GET /api/family/{familyId}/members/search - 搜索成员
```

### 4. 统计接口
```
GET /api/family/{familyId}/statistics - 获取家谱统计信息
```

### 5. 操作日志
```
GET /api/family/{familyId}/logs - 获取家谱操作日志
```

---

## 前端API调用示例

```javascript
import api from '@/utils/api'

// 登录
const loginRes = await api.auth.login('username', 'password')

// 获取我的家谱
const families = await api.family.getMine()

// 获取家谱树
const tree = await api.member.getTree(familyId)

// 申请修改信息
await api.member.applyEdit(familyId, memberId, {
  fieldName: 'bio',
  oldValue: '旧值',
  newValue: '新值'
})

// 处理审批
await api.approval.handle(familyId, requestId, {
  action: 'approve',
  remark: '同意'
})

// 获取所有待审批
const approvals = await api.approval.getAll({ status: 'pending' })
```
