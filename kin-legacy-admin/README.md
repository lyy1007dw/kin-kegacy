# kin-legacy-admin

家谱管理系统管理后台

## 技术栈

- Vue 3 + TypeScript + Vite
- Naive UI 组件库
- Pinia 状态管理
- Vue Router 4
- Axios

## 项目结构

```
src/
├── api/          # 接口定义
├── assets/       # 静态资源
├── components/   # 公共组件
├── layouts/     # 布局组件
│   └── BasicLayout.vue   # 带侧边栏的主布局
├── router/      # 路由配置
├── stores/      # Pinia store
├── types/       # TypeScript类型定义
├── utils/       # 工具函数
│   └── request.ts  # Axios封装
└── views/       # 页面
    ├── Login.vue      # 登录页
    ├── Dashboard.vue  # 首页
    ├── Family.vue     # 家谱管理
    ├── FamilyDetail.vue  # 家谱详情
    ├── Member.vue     # 成员管理
    ├── Approval.vue   # 审批管理
    └── User.vue       # 用户管理
```

## 启动说明

1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 默认管理员账号
- 用户名：admin
- 密码：123456

## 路由

| 路径 | 页面 | 说明 |
|------|------|------|
| /login | 登录页 | 登录页面 |
| / | 首页 | 数据概览 |
| /family | 家谱管理 | 家谱列表 |
| /family/:id | 家谱详情 | 家谱详情和成员 |
| /member | 成员管理 | 成员列表 |
| /approval | 审批管理 | 审批列表 |
| /user | 用户管理 | 用户列表 |

## 功能说明

### 审批管理
- 分两个 Tab：加入申请、修改申请
- 状态筛选：全部、待审批、已通过、已拒绝
- 操作：同意、拒绝（需二次确认）
- 分页支持
