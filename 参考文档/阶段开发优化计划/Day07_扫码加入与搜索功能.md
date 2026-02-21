# Day 7 - 扫码加入家谱与搜索功能

**开发日期**：第 7 天
**优先级**：P1（中高）
**所属阶段**：功能完善

---

## 一、功能需求描述

### 1.1 扫码加入家谱

实现家谱二维码生成和扫码加入功能，简化用户加入家谱的流程。

### 1.2 搜索功能

实现家谱名称搜索和成员姓名搜索，提升数据查找效率。

---

## 二、扫码加入家谱

### 2.1 功能流程

```
家谱管理员 -> 生成家谱二维码 -> 展示二维码
                                        ↓
新用户 -> 扫描二维码 -> 获取家谱信息 -> 确认加入 -> 提交申请/直接加入
```

### 2.2 二维码数据设计

#### 二维码内容格式

```json
{
  "type": "FAMILY_INVITE",
  "familyId": 1,
  "code": "ABC123",
  "exp": 1700000000
}
```

或使用简单URL格式：
```
https://kin.com/join?familyId=1&code=ABC123
```

### 2.3 后端接口设计

#### 生成邀请码

**接口路径**：`POST /api/family/{familyId}/invite-code`

**请求参数**：
```json
{
  "expireHours": 72
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "inviteCode": "ABC123",
    "qrCodeUrl": "https://xxx.com/qr/family_1_ABC123.png",
    "expireAt": "2026-02-24 10:00:00",
    "joinUrl": "https://kin.com/join?familyId=1&code=ABC123"
  }
}
```

#### 扫码获取家谱信息

**接口路径**：`GET /api/family/join-info`

**请求参数**：
- familyId: 家谱ID
- code: 邀请码

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "familyId": 1,
    "familyName": "张氏家谱",
    "memberCount": 50,
    "adminName": "张三",
    "familyDesc": "张氏家族家谱",
    "avatar": "https://xxx.com/family.jpg",
    "valid": true,
    "message": "邀请码有效"
  }
}
```

#### 申请加入家谱

**接口路径**：`POST /api/family/join`

**请求参数**：
```json
{
  "familyId": 1,
  "inviteCode": "ABC123",
  "memberName": "张四",
  "memberGender": "男",
  "relationMemberId": 2,
  "relationType": "father_son",
  "message": "我是张三的弟弟"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "申请已提交，等待审批",
  "data": {
    "requestId": 1,
    "status": "PENDING"
  }
}
```

### 2.4 邀请码管理

#### 邀请码表设计

```sql
CREATE TABLE family_invite_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  family_id BIGINT NOT NULL COMMENT '家谱ID',
  code VARCHAR(20) NOT NULL COMMENT '邀请码',
  creator_id BIGINT NOT NULL COMMENT '创建人ID',
  max_uses INT DEFAULT -1 COMMENT '最大使用次数，-1表示无限制',
  used_count INT DEFAULT 0 COMMENT '已使用次数',
  expire_at DATETIME COMMENT '过期时间',
  status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  INDEX idx_family_id (family_id)
) COMMENT '家谱邀请码表';
```

#### InviteCode 实体

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("family_invite_code")
public class FamilyInviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private String code;
    private Long creatorId;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime expireAt;
    private Integer status;
    private LocalDateTime createdAt;
}
```

### 2.5 二维码生成

#### 使用 Google ZXing

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>
```

#### QrCodeUtil

```java
@Component
public class QrCodeUtil {
    
    public String generateQrCode(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            
            BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            
            // 转Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
            
        } catch (Exception e) {
            throw new BusinessException("生成二维码失败");
        }
    }
}
```

### 2.6 后端实现

#### FamilyInviteService

```java
@Service
@RequiredArgsConstructor
public class FamilyInviteService {
    
    private final FamilyInviteCodeMapper inviteCodeMapper;
    private final FamilyMapper familyMapper;
    private final QrCodeUtil qrCodeUtil;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Transactional
    public InviteResult generateInviteCode(Long familyId, Integer expireHours) {
        // 生成6位邀请码
        String code = generateRandomCode(6);
        
        LocalDateTime expireAt = null;
        if (expireHours != null && expireHours > 0) {
            expireAt = LocalDateTime.now().plusHours(expireHours);
        }
        
        FamilyInviteCode inviteCode = FamilyInviteCode.builder()
            .familyId(familyId)
            .code(code)
            .creatorId(UserContext.getUserId())
            .maxUses(-1)
            .usedCount(0)
            .expireAt(expireAt)
            .status(1)
            .build();
        
        inviteCodeMapper.insert(inviteCode);
        
        String joinUrl = baseUrl + "/join?familyId=" + familyId + "&code=" + code;
        String qrCodeUrl = qrCodeUtil.generateQrCode(joinUrl, 300, 300);
        
        return InviteResult.builder()
            .inviteCode(code)
            .qrCodeUrl(qrCodeUrl)
            .expireAt(expireAt)
            .joinUrl(joinUrl)
            .build();
    }
    
    public JoinInfoVO getJoinInfo(Long familyId, String code) {
        // 验证邀请码
        FamilyInviteCode inviteCode = inviteCodeMapper.selectByCode(code);
        if (inviteCode == null || !inviteCode.getFamilyId().equals(familyId)) {
            return JoinInfoVO.builder().valid(false).message("邀请码无效").build();
        }
        
        if (inviteCode.getStatus() == 0) {
            return JoinInfoVO.builder().valid(false).message("邀请码已禁用").build();
        }
        
        if (inviteCode.getExpireAt() != null && LocalDateTime.now().isAfter(inviteCode.getExpireAt())) {
            return JoinInfoVO.builder().valid(false).message("邀请码已过期").build();
        }
        
        if (inviteCode.getMaxUses() > 0 && inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            return JoinInfoVO.builder().valid(false).message("邀请码已用完").build();
        }
        
        // 获取家谱信息
        Family family = familyMapper.selectById(familyId);
        
        return JoinInfoVO.builder()
            .familyId(familyId)
            .familyName(family.getName())
            .memberCount(family.getMemberCount())
            .valid(true)
            .message("邀请码有效")
            .build();
    }
    
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
```

---

## 三、搜索功能

### 3.1 家谱搜索

#### 接口设计

**接口路径**：`GET /api/family/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "records": [
      {
        "id": 1,
        "name": "张氏家谱",
        "code": "ABC123",
        "memberCount": 50,
        "creatorName": "张三",
        "createdAt": "2026-01-01"
      }
    ]
  }
}
```

### 3.2 成员搜索

#### 接口设计

**接口路径**：`GET /api/family/{familyId}/member/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词（姓名） |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "records": [
      {
        "id": 1,
        "name": "张三",
        "gender": "男",
        "birthDate": "1990-01-01",
        "avatar": "https://xxx.com/avatar.jpg"
      }
    ]
  }
}
```

### 3.3 全局成员搜索（管理员）

#### 接口设计

**接口路径**：`GET /api/admin/member/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| genealogyId | Long | 否 | 限定家谱 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |

### 3.4 后端实现

#### FamilyMapper.xml

```xml
<select id="searchFamily" resultType="com.kin.family.dto.FamilyVO">
    SELECT 
        f.id, f.name, f.code, f.member_count,
        u.nickname as creator_name, f.created_at
    FROM family f
    LEFT JOIN user u ON f.created_by = u.id
    WHERE f.name LIKE CONCAT('%', #{keyword}, '%')
    ORDER BY f.created_at DESC
</select>
```

#### MemberMapper.xml

```xml
<select id="searchMember" resultType="com.kin.family.dto.MemberVO">
    SELECT 
        id, name, gender, birth_date, avatar
    FROM family_member
    WHERE genealogy_id = #{familyId}
    AND name LIKE CONCAT('%', #{keyword}, '%')
    ORDER BY created_at DESC
</select>
```

### 3.5 搜索优化（可选）

#### 使用 Elasticsearch

```java
@Service
@RequiredArgsConstructor
public class MemberSearchService {
    
    private final ElasticsearchClient esClient;
    
    public PageResult<MemberDoc> search(String keyword, int page, int size) {
        SearchResponse<MemberDoc> response = esClient.search(s -> s
            .index("member")
            .query(q -> q
                .bool(b -> b
                    .should(s1 -> s1.match(m -> m.field("name").query(keyword)))
                    .should(s2 -> s2.match(m -> m.field("bio").query(keyword)))
                )
            )
            .from((page - 1) * size)
            .size(size),
            MemberDoc.class
        );
        
        List<MemberDoc> records = response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
        
        return new PageResult<>(response.hits().total().value(), page, size, records);
    }
}
```

---

## 四、小程序实现

### 4.1 邀请码页面

```vue
<template>
  <view class="invite-page">
    <view class="qrcode-wrapper">
      <image :src="qrCodeUrl" mode="widthFix" class="qrcode" />
    </view>
    <view class="invite-code">
      <text>邀请码：{{ inviteCode }}</text>
    </view>
    <view class="expire-info">
      <text v-if="expireAt">有效期至：{{ expireAt }}</text>
      <text v-else>永久有效</text>
    </view>
    <view class="actions">
      <button @click="copyInviteCode">复制邀请码</button>
      <button @click="shareToFriend">分享给好友</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      familyId: null,
      inviteCode: '',
      qrCodeUrl: '',
      expireAt: ''
    }
  },
  async onLoad(options) {
    this.familyId = options.familyId
    await this.generateInviteCode()
  },
  methods: {
    async generateInviteCode() {
      const res = await this.$api.post(`/api/family/${this.familyId}/invite-code`, {
        expireHours: 72
      })
      this.inviteCode = res.data.inviteCode
      this.qrCodeUrl = res.data.qrCodeUrl
      this.expireAt = res.data.expireAt
    },
    copyInviteCode() {
      uni.setClipboardData({
        data: this.inviteCode,
        success: () => {
          uni.showToast({ title: '复制成功', icon: 'success' })
        }
      })
    },
    shareToFriend() {
      // 小程序分享
    }
  }
}
</script>
```

### 4.2 扫码加入页面

```vue
<template>
  <view class="join-page">
    <view class="family-info" v-if="familyInfo">
      <image :src="familyInfo.avatar" class="avatar" />
      <text class="name">{{ familyInfo.familyName }}</text>
      <text class="member-count">{{ familyInfo.memberCount }} 位成员</text>
    </view>
    
    <view class="form" v-if="familyInfo && familyInfo.valid">
      <view class="form-item">
        <text>姓名</text>
        <input v-model="form.memberName" placeholder="请输入您的姓名" />
      </view>
      <view class="form-item">
        <text>性别</text>
        <picker :value="genderIndex" :range="genders" @change="onGenderChange">
          <view class="picker">{{ genders[genderIndex] }}</view>
        </picker>
      </view>
      
      <button class="join-btn" @click="submitJoin">申请加入</button>
    </view>
    
    <view class="invalid" v-if="familyInfo && !familyInfo.valid">
      <text>{{ familyInfo.message }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      familyId: null,
      inviteCode: '',
      familyInfo: null,
      form: {
        memberName: '',
        memberGender: '男'
      },
      genders: ['男', '女'],
      genderIndex: 0
    }
  },
  async onLoad(options) {
    this.familyId = options.familyId
    this.inviteCode = options.code
    await this.getJoinInfo()
  },
  methods: {
    async getJoinInfo() {
      const res = await this.$api.get('/api/family/join-info', {
        familyId: this.familyId,
        code: this.inviteCode
      })
      this.familyInfo = res.data
    },
    async submitJoin() {
      if (!this.form.memberName) {
        return uni.showToast({ title: '请输入姓名', icon: 'none' })
      }
      
      const res = await this.$api.post('/api/family/join', {
        familyId: this.familyId,
        inviteCode: this.inviteCode,
        memberName: this.form.memberName,
        memberGender: this.form.memberGender
      })
      
      uni.showToast({ title: '申请已提交', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 1500)
    }
  }
}
</script>
```

### 4.3 搜索页面

```vue
<template>
  <view class="search-page">
    <view class="search-bar">
      <input 
        v-model="keyword" 
        placeholder="搜索成员姓名" 
        @confirm="handleSearch"
      />
      <button @click="handleSearch">搜索</button>
    </view>
    
    <view class="result-list">
      <view 
        class="member-item" 
        v-for="item in resultList" 
        :key="item.id"
        @click="goToMemberDetail(item.id)"
      >
        <image :src="item.avatar || defaultAvatar" class="avatar" />
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <text class="desc">{{ item.gender }} · {{ item.birthDate }}</text>
        </view>
      </view>
    </view>
  </view>
</template>
```

---

## 五、测试用例

### 5.1 邀请码测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 生成邀请码 | 调用生成接口 | 返回邀请码和二维码 |
| 有效邀请码 | 使用有效码加入 | 显示家谱信息 |
| 过期邀请码 | 使用过期码 | 提示已过期 |
| 无效邀请码 | 使用错误码 | 提示无效 |

### 5.2 搜索测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 精确搜索 | 搜索"张三" | 返回张三 |
| 模糊搜索 | 搜索"张" | 返回所有姓张的成员 |
| 无结果搜索 | 搜索"不存在的人" | 返回空列表 |

---

## 六、交付物

- [ ] FamilyInviteCode 实体和 Mapper
- [ ] 二维码生成工具
- [ ] 邀请码生成接口
- [ ] 扫码获取家谱信息接口
- [ ] 申请加入接口
- [ ] 家谱搜索接口
- [ ] 成员搜索接口
- [ ] 小程序邀请码页面
- [ ] 小程序扫码加入页面
- [ ] 小程序搜索页面
- [ ] 单元测试代码
