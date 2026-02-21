# Day 03.5 - 用户与成员姓名一致性

**开发日期**：第 3.5 天
**优先级**：P1（中高）
**所属阶段**：功能完善

---

## 一、功能需求描述

### 1.1 背景

微信用户可作为不同家谱的多个成员，用户的昵称和姓名应该保持一致，避免姓名不统一导致查找困难。

### 1.2 功能目标

- 用户真实姓名全局统一
- 修改用户姓名时自动同步到所有家谱成员
- 管理员无法直接修改关联用户的成员姓名
- 新增关联用户成员时自动填充用户姓名

---

## 二、数据库设计

### 2.1 User 表改造

确保 `name` 字段存在且可用：

```sql
ALTER TABLE user ADD COLUMN name VARCHAR(50) COMMENT '真实姓名（全局统一）';
ALTER TABLE user ADD UNIQUE INDEX uk_name (name);
```

### 2.2 Member 表

确保 `user_id` 字段存在：

```sql
ALTER TABLE family_member ADD COLUMN user_id BIGINT COMMENT '关联用户ID';
ALTER TABLE family_member ADD INDEX idx_user_id (user_id);
```

---

## 三、后端实现

### 3.1 UserService 改造

```java
/**
 * 更新用户姓名并同步到所有家谱成员
 */
@Transactional(rollbackFor = Exception.class)
public void updateUserName(Long userId, String newName) {
    // 1. 校验姓名唯一性
    User existUser = userMapper.selectByName(newName);
    if (existUser != null && !existUser.getId().equals(userId)) {
        throw new BusinessException("该姓名已被其他用户使用");
    }
    
    // 2. 获取旧姓名用于日志
    User oldUser = userMapper.selectById(userId);
    String oldName = oldUser.getName();
    
    // 3. 更新用户姓名
    oldUser.setName(newName);
    userMapper.updateById(oldUser);
    
    // 4. 同步更新所有家谱中的成员姓名
    List<FamilyMember> members = memberMapper.selectList(
        new LambdaQueryWrapper<FamilyMember>()
            .eq(FamilyMember::getUserId, userId)
    );
    
    for (FamilyMember member : members) {
        member.setName(newName);
        memberMapper.updateById(member);
    }
    
    // 5. 清除缓存
    evictUserCache(userId);
    for (FamilyMember member : members) {
        evictMemberCache(member.getGenealogyId());
    }
    
    // 6. 记录日志
    log.info("用户姓名修改：{} -> {}, 同步更新了 {} 个成员", oldName, newName, members.size());
}
```

### 3.2 MemberService 改造

```java
/**
 * 新增成员时，如果关联了用户，自动填充用户姓名
 */
public FamilyMember addMember(AddMemberRequest request, Long familyId) {
    // 如果关联了用户，自动填充用户姓名
    if (request.getUserId() != null) {
        User user = userMapper.selectById(request.getUserId());
        if (user != null && user.getName() != null) {
            request.setName(user.getName());
        }
    }
    
    // 校验成员姓名是否与关联用户姓名一致
    if (request.getUserId() != null && request.getName() != null) {
        User user = userMapper.selectById(request.getUserId());
        if (user != null && !request.getName().equals(user.getName())) {
            throw new BusinessException("成员姓名必须与用户姓名一致");
        }
    }
    
    // ... 后续逻辑
}
```

### 3.3 修改成员姓名时校验

```java
/**
 * 更新成员信息
 */
public void updateMember(Long memberId, UpdateMemberRequest request) {
    FamilyMember member = memberMapper.selectById(memberId);
    
    // 如果成员关联了用户，禁止直接修改姓名
    if (member.getUserId() != null && request.getName() != null) {
        User user = userMapper.selectById(member.getUserId());
        if (!request.getName().equals(user.getName())) {
            throw new BusinessException("该成员已关联用户账号，姓名不允许直接修改，请通过用户管理修改");
        }
    }
    
    // ... 后续逻辑
}
```

---

## 四、接口设计

### 4.1 修改用户姓名

**接口路径**：`PUT /api/user/name`

**请求头**：`Authorization: Bearer {token}`

**请求参数**：
```json
{
  "name": "张三"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "姓名修改成功，已同步到所有家谱",
  "data": null
}
```

**错误响应**：
```json
{
  "code": 400,
  "message": "该姓名已被其他用户使用",
  "data": null
}
```

### 4.2 获取用户真实姓名

**接口路径**：`GET /api/user/{userId}/name`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "name": "张三"
  }
}
```

---

## 五、前端实现

### 5.1 用户端 - 修改姓名

```vue
<template>
  <div class="profile-page">
    <n-form-item label="真实姓名">
      <n-input v-model:value="form.name" placeholder="请输入真实姓名" />
      <n-alert type="info" style="margin-top: 8px">
        修改后将同步更新您所在所有家谱中的姓名
      </n-alert>
    </n-form-item>
  </div>
</template>

<script setup>
const handleUpdateName = async () => {
  await $dialog.warning({
    title: '确认修改',
    content: '修改后将同步更新您所在所有家谱中的姓名，确定要修改吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await api.put('/api/user/name', { name: form.value.name })
      $message.success('姓名修改成功')
    }
  })
}
</script>
```

### 5.2 管理后台 - 成员编辑

```vue
<template>
  <n-form-item label="姓名">
    <n-input 
      v-model:value="form.name" 
      :disabled="!!form.userId"
    />
    <n-alert v-if="form.userId" type="warning" style="margin-top: 8px">
      该成员已关联用户账号，姓名不允许直接修改
    </n-alert>
  </n-form-item>
</template>
```

---

## 六、数据迁移

### 6.1 同步现有用户和成员姓名

```sql
-- 将已有成员的姓名同步到用户表
UPDATE user u
JOIN family_member fm ON fm.user_id = u.id
SET u.name = fm.name
WHERE u.name IS NULL OR u.name = '';

-- 或者反向同步（以用户姓名为准）
UPDATE family_member fm
JOIN user u ON fm.user_id = u.id
SET fm.name = u.name
WHERE u.name IS NOT NULL AND u.name != '';
```

---

## 七、测试用例

| 测试项 | 场景 | 预期结果 |
|--------|------|----------|
| 修改用户姓名 | 用户修改姓名为"张三" | 同步更新所有家谱中的成员姓名 |
| 重复姓名 | 修改为已存在的姓名 | 提示"该姓名已被使用" |
| 管理员修改 | 管理员修改关联用户的成员姓名 | 提示不允许直接修改 |
| 新增成员 | 新增关联用户的成员 | 自动填充用户姓名 |

---

## 八、交付物

- [ ] UserService 姓名同步方法
- [ ] MemberService 新增/修改校验
- [ ] 修改用户姓名接口
- [ ] 前端用户端修改姓名页面
- [ ] 前端管理后台成员编辑限制
- [ ] 数据迁移脚本

---

## 九、注意事项

1. 姓名全局唯一性校验
2. 修改前弹窗提示用户
3. 记录操作日志
4. 清除相关缓存
5. 事务保证数据一致性
