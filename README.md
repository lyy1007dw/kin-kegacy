<p align="center">
  <h1 align="center">传承谱</h1>
  <p align="center">
    <em>让家族记忆永续传承</em>
  </p>
  <p align="center">
    <a href="#功能特性">功能特性</a> •
    <a href="#技术栈">技术栈</a> •
    <a href="#快速开始">快速开始</a> •
    <a href="#项目结构">项目结构</a>
  </p>
</p>

---

## 项目简介

**传承谱**是一个现代化的家谱管理系统，旨在帮助家族记录、管理和传承家族历史。通过树状可视化展示、权限分级管理和审批流程，让每一位家族成员都能参与到家谱的维护中来。

## 功能特性

### 核心功能

- **家谱树展示** - 直观的树状结构展示家族成员关系
- **成员管理** - 完整的家族成员信息录入与维护
- **权限分级** - 支持超级管理员、家谱管理员、普通用户三种角色
- **审批流程** - 成员加入、信息修改等操作需经过审批

### 系统特点

- 响应式设计，支持多端访问
- 微信小程序移动端支持
- 现代化的后台管理界面
- 完善的权限控制体系

## 技术栈

### 后端服务

- **框架**: Spring Boot 2.x
- **ORM**: MyBatis Plus
- **数据库**: MySQL
- **认证**: JWT Token

### 前端技术

**小程序端**
- uni-app
- Vue 3
- Vuex

**管理后台**
- Vue 3
- TypeScript
- Vite
- Element Plus

## 项目结构

```
kin-legacy/
├── kin-legacy-backend/    # 后端服务 (Spring Boot)
├── kin-legacy-mp/         # 微信小程序 (uni-app)
├── kin-legacy-admin/      # 管理后台 (Vue 3 + TypeScript)
└── 参考文档/              # 项目需求文档
```

## 快速开始

### 环境要求

- JDK 1.8+
- Node.js 14+
- MySQL 5.7+
- Maven 3.6+

### 后端启动

```bash
cd kin-legacy-backend
# 配置数据库连接信息
# 修改 src/main/resources/application.yml
mvn spring-boot:run
```

### 前端启动

**管理后台**
```bash
cd kin-legacy-admin
npm install
npm run dev
```

**小程序**
```bash
cd kin-legacy-mp
npm install
# 使用 HBuilderX 或微信开发者工具打开项目
```

## 核心模块

| 模块 | 说明 |
|------|------|
| 用户管理 | 用户注册、登录、角色权限管理 |
| 家谱管理 | 家谱创建、信息维护、成员关系管理 |
| 成员管理 | 成员信息录入、关系树展示、信息修改 |
| 审批管理 | 加入申请审批、信息修改审批 |

## 版本历史

- **v1.0.0** - 初始版本发布

## 贡献指南

欢迎提交 Issue 和 Pull Request 参与项目开发。

## 许可证

[MIT License](LICENSE)

## 作者

**lyy1007dw**

---

<p align="center">
  <sub>用心记录，让爱传承</sub>
</p>
