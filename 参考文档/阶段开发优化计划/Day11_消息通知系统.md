# Day 11 - 消息通知系统

**开发日期**：第 11 天
**优先级**：P3（较低）
**所属阶段**：体验优化

---

## 一、功能需求描述

### 1.1 背景

系统需要完善的消息通知功能，包括站内信、微信小程序消息推送等。

### 1.2 通知场景

| 通知场景 | 通知方式 |
|----------|----------|
| 审批通过/拒绝 | 小程序消息 + 站内信 |
| 新成员加入 | 站内信 |
| 家谱邀请 | 小程序消息 |
| 系统公告 | 站内信 |

---

## 二、数据库设计

### 2.1 站内信表

```sql
CREATE TABLE user_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '接收用户ID',
  title VARCHAR(100) NOT NULL COMMENT '消息标题',
  content TEXT NOT NULL COMMENT '消息内容',
  type VARCHAR(20) NOT NULL COMMENT '消息类型: APPROVAL/APPOINT/SYSTEM',
  related_id BIGINT COMMENT '关联ID（如审批ID）',
  related_type VARCHAR(50) COMMENT '关联类型',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  read_at DATETIME COMMENT '阅读时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_is_read (is_read),
  INDEX idx_created_at (created_at)
) COMMENT '用户消息表';
```

### 2.2 消息订阅配置表

```sql
CREATE TABLE message_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(20) NOT NULL COMMENT '消息类型',
  title VARCHAR(100) NOT NULL COMMENT '标题模板',
  content TEXT NOT NULL COMMENT '内容模板',
  wechat_template_id VARCHAR(100) COMMENT '微信模板ID',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_type (type)
) COMMENT '消息模板表';

-- 初始化模板
INSERT INTO message_template (type, title, content, wechat_template_id) VALUES
('APPROVAL_PASS', '审批通过', '您的「${familyName}」成员${memberName}的申请已通过', 'xxx'),
('APPROVAL_REJECT', '审批拒绝', '您的「${familyName}」成员${memberName}的申请已被拒绝', 'xxx'),
('MEMBER_JOIN', '新成员加入', '「${familyName}」新增成员${memberName}', ''),
('FAMILY_INVITE', '家谱邀请', '您被邀请加入「${familyName}」', 'xxx'),
('SYSTEM_NOTICE', '系统公告', '${content}', '');
```

---

## 三、后端实现

### 3.1 消息实体

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_message")
public class UserMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private String relatedType;
    private Integer isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
```

### 3.2 消息服务

```java
public interface MessageService {
    
    void sendMessage(Long userId, String type, String title, String content);
    
    void sendMessage(Long userId, String type, String title, String content, 
                     Long relatedId, String relatedType);
    
    PageResult<UserMessage> getMessageList(Long userId, String type, 
                                           Integer isRead, Integer page, Integer size);
    
    void markAsRead(Long messageId);
    
    void markAllAsRead(Long userId);
    
    int getUnreadCount(Long userId);
}
```

### 3.3 消息服务实现

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    
    private final UserMessageMapper messageMapper;
    private final WechatMessageService wechatMessageService;
    
    @Override
    public void sendMessage(Long userId, String type, String title, String content) {
        sendMessage(userId, type, title, content, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long userId, String type, String title, String content,
                            Long relatedId, String relatedType) {
        // 保存站内信
        UserMessage message = UserMessage.builder()
            .userId(userId)
            .type(type)
            .title(title)
            .content(content)
            .relatedId(relatedId)
            .relatedType(relatedType)
            .isRead(0)
            .build();
        
        messageMapper.insert(message);
        
        // 发送微信小程序消息
        try {
            wechatMessageService.sendNotification(userId, type, title, content);
        } catch (Exception e) {
            log.error("发送微信消息失败", e);
        }
    }
    
    @Override
    public PageResult<UserMessage> getMessageList(Long userId, String type,
                                                   Integer isRead, Integer page, Integer size) {
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);
        if (type != null) {
            wrapper.eq(UserMessage::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(UserMessage::getIsRead, isRead);
        }
        wrapper.orderByDesc(UserMessage::getCreatedAt);
        
        Page<UserMessage> pageResult = messageMapper.selectPage(
            new Page<>(page, size), wrapper);
        
        return PageResult.of(pageResult);
    }
    
    @Override
    public void markAsRead(Long messageId) {
        UserMessage message = messageMapper.selectById(messageId);
        if (message != null && message.getIsRead() == 0) {
            message.setIsRead(1);
            message.setReadAt(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<UserMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId)
            .eq(UserMessage::getIsRead, 0)
            .set(UserMessage::getIsRead, 1)
            .set(UserMessage::getReadAt, LocalDateTime.now());
        messageMapper.update(null, wrapper);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.selectCount(
            new LambdaQueryWrapper<UserMessage>()
                .eq(UserMessage::getUserId, userId)
                .eq(UserMessage::getIsRead, 0)
        );
    }
}
```

### 3.4 审批通知

```java
@Service
@RequiredArgsConstructor
public class ApprovalNotificationService {
    
    private final MessageService messageService;
    private final UserMapper userMapper;
    
    public void notifyApprovalResult(Approval approval, boolean approved) {
        // 获取申请人
        User applicant = userMapper.selectById(approval.getUserId());
        if (applicant == null) return;
        
        // 获取家谱信息
        String familyName = getFamilyName(approval.getFamilyId());
        String memberName = approval.getMemberName();
        
        String type = approved ? "APPROVAL_PASS" : "APPROVAL_REJECT";
        String title = approved ? "审批通过" : "审批拒绝";
        String content = approved 
            ? String.format("您的「%s」成员%s的申请已通过", familyName, memberName)
            : String.format("您的「%s」成员%s的申请已被拒绝，原因是：%s", 
                familyName, memberName, approval.getRejectReason());
        
        messageService.sendMessage(
            applicant.getId(),
            type,
            title,
            content,
            approval.getId(),
            "APPROVAL"
        );
    }
}
```

---

## 四、微信小程序消息

### 4.1 依赖引入

```xml
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-mp</artifactId>
    <version>4.5.0</version>
</dependency>
```

### 4.2 配置

```yaml
# application.yml
wechat:
  mp:
    appId: wx1234567890
    appSecret: xxx
    token: xxx
    aesKey: xxx
```

### 4.3 微信消息服务

```java
@Service
@RequiredArgsConstructor
public class WechatMessageService {
    
    private final WxMpService wxMpService;
    private final UserMapper userMapper;
    
    public void sendNotification(Long userId, String type, String title, String content) {
        // 获取用户微信OpenID
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) {
            return;
        }
        
        // 获取模板ID
        String templateId = getTemplateId(type);
        if (templateId == null) {
            return;
        }
        
        try {
            WxMpTemplateMessage message = new WxMpTemplateMessage();
            message.setToUser(user.getOpenid());
            message.setTemplateId(templateId);
            message.setUrl("https://kin.com/message/" + type);
            
            // 设置模板内容
            message.addData(new WxMpTemplateData("first", title, "#173177"));
            message.addData(new WxMpTemplateData("keyword1", content, "#173177"));
            message.addData(new WxMpTemplateData("keyword2", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 
                "#999999"));
            message.addData(new WxMpTemplateData("remark", "点击查看详情", "#999999"));
            
            wxMpService.getTemplateMsgService().sendTemplateMsg(message);
        } catch (Exception e) {
            log.error("发送微信模板消息失败", e);
        }
    }
    
    private String getTemplateId(String type) {
        // 从数据库或缓存获取
        return messageTemplateMapper.selectByType(type)?.getWechatTemplateId();
    }
}
```

---

## 五、接口设计

### 5.1 获取消息列表

**接口路径**：`GET /api/message`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 消息类型 |
| isRead | Integer | 否 | 已读状态 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "title": "审批通过",
        "content": "您的成员申请已通过",
        "type": "APPROVAL_PASS",
        "isRead": 0,
        "createdAt": "2026-02-21 10:00:00"
      }
    ]
  }
}
```

### 5.2 标记已读

**接口路径**：`PUT /api/message/{id}/read`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 5.3 获取未读数量

**接口路径**：`GET /api/message/unread-count`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

---

## 六、前端实现

### 6.1 消息列表页面

```vue
<template>
  <div class="message-page">
    <view class="tabs">
      <view 
        :class="['tab', currentType === '' ? 'active' : '']"
        @click="switchType('')"
      >全部</view>
      <view 
        :class="['tab', currentType === 'APPROVAL' ? 'active' : '']"
        @click="switchType('APPROVAL')"
      >审批</view>
      <view 
        :class="['tab', currentType === 'SYSTEM' ? 'active' : '']"
        @click="switchType('SYSTEM')"
      >系统</view>
    </view>
    
    <view class="message-list">
      <view 
        v-for="item in messageList" 
        :key="item.id"
        :class="['message-item', item.isRead ? 'read' : 'unread']"
        @click="handleClick(item)"
      >
        <view class="title">{{ item.title }}</view>
        <view class="content">{{ item.content }}</view>
        <view class="time">{{ item.createdAt }}</view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentType: '',
      messageList: []
    }
  },
  async onLoad() {
    await this.loadMessageList()
  },
  methods: {
    async loadMessageList() {
      const res = await this.$api.get('/api/message', {
        type: this.currentType || undefined,
        page: 1,
        size: 20
      })
      this.messageList = res.data.records
    },
    async handleClick(item) {
      if (!item.isRead) {
        await this.$api.put(`/api/message/${item.id}/read`)
      }
      // 跳转详情或处理
    }
  }
}
</script>
```

---

## 七、交付物

- [ ] UserMessage 实体和 Mapper
- [ ] MessageTemplate 实体和 Mapper
- [ ] MessageService 消息服务
- [ ] WechatMessageService 微信消息服务
- [ ] 消息列表接口
- [ ] 标记已读接口
- [ ] 小程序消息通知页面

---

## 八、注意事项

1. 微信小程序消息需要用户先订阅
2. 模板消息需要提前在微信后台配置
3. 消息发送失败不影响主业务流程
4. 定期清理过期消息
