# Day 05.5 - 数据库索引优化

**开发日期**：第 5.5 天（补充）
**优先级**：P2
**所属阶段**：性能优化

---

## 一、功能需求描述

### 1.1 背景

数据库查询性能直接影响系统响应速度，需要添加必要索引优化查询效率。

### 1.2 目标

- 添加必要索引
- 优化分页查询
- 监控慢查询

---

## 二、索引设计

### 2.1 用户表索引

```sql
-- 已有或需添加
ALTER TABLE user ADD INDEX idx_username (username);
ALTER TABLE user ADD INDEX idx_openid (openid);
ALTER TABLE user ADD INDEX idx_global_role (global_role);
ALTER TABLE user ADD INDEX idx_phone (phone);
```

### 2.2 家谱表索引

```sql
ALTER TABLE family ADD INDEX idx_code (code);
ALTER TABLE family ADD INDEX idx_created_by (created_by);
ALTER TABLE family ADD INDEX idx_name (name);
```

### 2.3 成员表索引

```sql
ALTER TABLE family_member ADD INDEX idx_genealogy_id (genealogy_id);
ALTER TABLE family_member ADD INDEX idx_name (name);
ALTER TABLE family_member ADD INDEX idx_birth_date (birth_date);
ALTER TABLE family_member ADD INDEX idx_user_id (user_id);
ALTER TABLE family_member ADD INDEX idx_created_at (created_at);
-- 复合索引
ALTER TABLE family_member ADD INDEX idx_genealogy_name (genealogy_id, name);
```

### 2.4 关系表索引

```sql
ALTER TABLE member_relation ADD INDEX idx_from_member (from_member_id);
ALTER TABLE member_relation ADD INDEX idx_to_member (to_member_id);
ALTER TABLE member_relation ADD INDEX idx_genealogy (genealogy_id);
```

### 2.5 审批表索引

```sql
ALTER TABLE join_request ADD INDEX idx_genealogy_id (genealogy_id);
ALTER TABLE join_request ADD INDEX idx_user_id (user_id);
ALTER TABLE join_request ADD INDEX idx_status (status);
ALTER TABLE join_request ADD INDEX idx_created_at (created_at);
```

### 2.6 用户-家谱关联表索引

```sql
ALTER TABLE user_genealogy ADD INDEX idx_user_id (user_id);
ALTER TABLE user_genealogy ADD INDEX idx_genealogy_id (genealogy_id);
ALTER TABLE user_genealogy ADD INDEX idx_role (role);
-- 复合唯一索引
ALTER TABLE user_genealogy ADD UNIQUE INDEX uk_user_genealogy (user_id, genealogy_id);
```

---

## 三、慢查询监控

### 3.1 MySQL 配置

```sql
-- 查看慢查询日志配置
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';

-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
```

### 3.2 分析慢查询

```sql
-- 查看慢查询日志
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- 使用 EXPLAIN 分析查询
EXPLAIN SELECT * FROM family_member 
WHERE genealogy_id = 1 AND name LIKE '%张%';
```

---

## 四、交付物

- [ ] 索引 SQL 脚本
- [ ] 慢查询监控配置
