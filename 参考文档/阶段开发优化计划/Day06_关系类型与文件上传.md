# Day 6 - 关系类型完善与文件上传功能

**开发日期**：第 6 天
**优先级**：P1（中高）
**所属阶段**：功能完善

---

## 一、功能需求描述

### 1.1 关系类型完善

当前系统关系类型不完整，需要补充更全面的成员关系类型，以支持更复杂的家谱结构。

### 1.2 文件上传功能

实现头像上传功能，支持本地存储和OSS云存储两种方案，为后续功能提供基础支撑。

---

## 二、关系类型完善

### 2.1 现有关系类型

| 关系类型 | 说明 |
|----------|------|
| father_son | 父子 |
| mother_son | 母子 |

### 2.2 新增关系类型

| 关系类型 | 说明 |
|----------|------|
| father_daughter | 父女 |
| mother_daughter | 母女 |
| spouse | 配偶 |
| sibling | 兄弟姐妹 |

### 2.3 关系类型枚举改造

```java
public enum RelationType {
    FATHER_SON("father_son", "父子"),
    FATHER_DAUGHTER("father_daughter", "父女"),
    MOTHER_SON("mother_son", "母子"),
    MOTHER_DAUGHTER("mother_daughter", "母女"),
    SPOUSE("spouse", "配偶"),
    SIBLING("sibling", "兄弟姐妹");
    
    private final String code;
    private final String desc;
    
    // 获取反向关系
    public RelationType reverse() {
        switch (this) {
            case FATHER_SON: return null;  // 儿子对父亲
            case FATHER_DAUGHTER: return null;  // 女儿对父亲
            case MOTHER_SON: return null;
            case MOTHER_DAUGHTER: return null;
            case SPOUSE: return SPOUSE;
            case SIBLING: return SIBLING;
            default: return null;
        }
    }
    
    // 根据性别获取关系类型
    public static RelationType getParentChildRelation(String parentGender, String childGender) {
        if ("男".equals(parentGender)) {
            return "男".equals(childGender) ? FATHER_SON : FATHER_DAUGHTER;
        } else {
            return "男".equals(childGender) ? MOTHER_SON : MOTHER_DAUGHTER;
        }
    }
}
```

### 2.4 数据库迁移

```sql
-- 更新现有关系数据
UPDATE member_relation 
SET relation_type = 'father_son' 
WHERE relation_type = 'father' OR relation_type = '父子';

UPDATE member_relation 
SET relation_type = 'mother_son' 
WHERE relation_type = 'mother' OR relation_type = '母子';
```

### 2.5 接口设计

#### 新增成员关系

**接口路径**：`POST /api/family/{familyId}/relation`

**请求参数**：
```json
{
  "fromMemberId": 1,
  "toMemberId": 2,
  "relationType": "father_son"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "fromMemberId": 1,
    "toMemberId": 2,
    "relationType": "father_son",
    "relationDesc": "父子"
  }
}
```

#### 获取成员关系列表

**接口路径**：`GET /api/family/{familyId}/member/{memberId}/relations`

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "relatedMemberId": 2,
      "relatedMemberName": "张父",
      "relationType": "father_son",
      "relationDesc": "父亲",
      "direction": "up"
    },
    {
      "id": 2,
      "relatedMemberId": 3,
      "relatedMemberName": "张妻",
      "relationType": "spouse",
      "relationDesc": "配偶",
      "direction": "peer"
    },
    {
      "id": 3,
      "relatedMemberId": 4,
      "relatedMemberName": "张儿",
      "relationType": "father_son",
      "relationDesc": "儿子",
      "direction": "down"
    }
  ]
}
```

---

## 三、文件上传功能

### 3.1 技术方案

#### 本地存储方案

- 文件存储路径：`/data/upload/`
- URL访问路径：`/upload/{year}/{month}/{filename}`
- 文件命名：`{UUID}.{ext}`

#### OSS云存储方案

- 使用阿里云OSS或腾讯云COS
- 配置Bucket、AccessKey等
- 支持私有/公开访问

### 3.2 配置文件

```yaml
# application.yml
file:
  upload:
    type: local  # local 或 oss
    local:
      path: /data/upload
      url-prefix: http://localhost:8080/upload/
    oss:
      endpoint: oss-cn-beijing.aliyuncs.com
      bucket-name: kin-legacy
      access-key-id: ${OSS_ACCESS_KEY_ID}
      access-key-secret: ${OSS_ACCESS_KEY_SECRET}
      url-prefix: https://kin-legacy.oss-cn-beijing.aliyuncs.com/
  allowed-types: jpg,jpeg,png,gif,webp
  max-size: 5242880  # 5MB
```

### 3.3 后端接口设计

#### 文件上传接口

**接口路径**：`POST /api/file/upload`

**请求参数**：
- Content-Type: `multipart/form-data`
- file: 文件二进制数据
- type: 上传类型（avatar、photo等）

**响应结构**：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": "abc123",
    "fileName": "avatar.jpg",
    "fileSize": 102400,
    "fileUrl": "https://xxx.com/upload/2026/02/abc123.jpg",
    "thumbnailUrl": "https://xxx.com/upload/2026/02/abc123_thumb.jpg"
  }
}
```

#### 文件删除接口

**接口路径**：`DELETE /api/file/{fileId}`

**响应结构**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 3.4 后端实现

#### FileService

```java
public interface FileService {
    UploadResult upload(MultipartFile file, String type);
    void delete(String fileId);
    String getAccessUrl(String fileId);
}

@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {
    
    @Value("${file.upload.local.path}")
    private String uploadPath;
    
    @Value("${file.upload.local.url-prefix}")
    private String urlPrefix;
    
    @Override
    public UploadResult upload(MultipartFile file, String type) {
        // 校验文件类型
        String ext = getFileExtension(file.getOriginalFilename());
        validateFileType(ext);
        
        // 校验文件大小
        validateFileSize(file.getSize());
        
        // 生成文件名
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String fileName = fileId + "." + ext;
        
        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fullPath = uploadPath + "/" + datePath;
        
        // 创建目录
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 保存文件
        File targetFile = new File(fullPath + "/" + fileName);
        file.transferTo(targetFile);
        
        // 图片压缩生成缩略图
        if (isImage(ext)) {
            generateThumbnail(targetFile, fullPath, fileId, ext);
        }
        
        return UploadResult.builder()
            .fileId(fileId)
            .fileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .fileUrl(urlPrefix + datePath + "/" + fileName)
            .thumbnailUrl(urlPrefix + datePath + "/" + fileId + "_thumb." + ext)
            .build();
    }
    
    private void generateThumbnail(File source, String path, String fileId, String ext) {
        try {
            BufferedImage image = ImageIO.read(source);
            int width = 200;
            int height = (int) ((double) image.getHeight() / image.getWidth() * width);
            
            BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
            
            File thumbFile = new File(path + "/" + fileId + "_thumb." + ext);
            ImageIO.write(thumbnail, ext, thumbFile);
        } catch (IOException e) {
            log.error("生成缩略图失败", e);
        }
    }
}
```

#### OSS 实现

```java
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {
    
    @Value("${file.upload.oss.endpoint}")
    private String endpoint;
    
    @Value("${file.upload.oss.bucket-name}")
    private String bucketName;
    
    private final OSS ossClient;
    
    @Override
    public UploadResult upload(MultipartFile file, String type) {
        String ext = getFileExtension(file.getOriginalFilename());
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String fileName = "upload/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")) 
            + "/" + fileId + "." + ext;
        
        try {
            ossClient.putObject(bucketName, fileName, file.getInputStream());
            
            return UploadResult.builder()
                .fileId(fileId)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileUrl("https://" + bucketName + "." + endpoint + "/" + fileName)
                .build();
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
    }
    
    @Override
    public void delete(String fileId) {
        // 查找并删除文件
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        request.setPrefix("upload/");
        ObjectListing listing = ossClient.listObjects(request);
        
        for (OSSObjectSummary summary : listing.getObjectSummaries()) {
            if (summary.getKey().contains(fileId)) {
                ossClient.deleteObject(bucketName, summary.getKey());
            }
        }
    }
}
```

#### FileController

```java
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    @RequireLogin
    public Result<UploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {
        UploadResult result = fileService.upload(file, type);
        return Result.success(result);
    }
    
    @DeleteMapping("/{fileId}")
    @RequireLogin
    public Result<Void> delete(@PathVariable String fileId) {
        fileService.delete(fileId);
        return Result.success();
    }
}
```

### 3.5 静态资源配置

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.local.path}")
    private String uploadPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:" + uploadPath + "/");
    }
}
```

---

## 四、头像上传集成

### 4.1 用户头像更新

**接口路径**：`PUT /api/user/avatar`

**请求参数**：
```json
{
  "avatar": "https://xxx.com/upload/2026/02/abc123.jpg"
}
```

### 4.2 成员头像更新

**接口路径**：`PUT /api/family/{familyId}/member/{memberId}/avatar`

**请求参数**：
```json
{
  "avatar": "https://xxx.com/upload/2026/02/abc123.jpg"
}
```

---

## 五、前端实现

### 5.1 文件上传组件

```vue
<template>
  <div class="avatar-upload">
    <n-upload
      :action="uploadUrl"
      :headers="headers"
      :max="1"
      accept="image/*"
      @before-upload="beforeUpload"
      @finish="handleFinish"
    >
      <n-upload-trigger #="{ handleClick }">
        <div class="avatar-wrapper" @click="handleClick">
          <n-avatar :src="modelValue" :size="100" />
          <div class="upload-mask">
            <n-icon><CameraOutline /></n-icon>
            <span>上传头像</span>
          </div>
        </div>
      </n-upload-trigger>
    </n-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { getToken } from '@/utils/auth'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/file/upload'
const headers = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const beforeUpload = (data: { file: UploadFileInfo }) => {
  const isImage = data.file.file?.type.startsWith('image/')
  const isLt5M = (data.file.file?.size || 0) / 1024 / 1024 < 5
  
  if (!isImage) {
    window.$message.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    window.$message.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleFinish = ({ event }: { event: ProgressEvent }) => {
  const response = JSON.parse((event.target as XMLHttpRequest).response)
  if (response.code === 200) {
    emit('update:modelValue', response.data.fileUrl)
    window.$message.success('上传成功')
  }
}
</script>
```

### 5.2 小程序上传

```javascript
// utils/upload.js
export function uploadFile(filePath, type = 'avatar') {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/api/file/upload',
      filePath: filePath,
      name: 'file',
      formData: { type },
      header: {
        'Authorization': `Bearer ${uni.getStorageSync('accessToken')}`
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)
        } else {
          reject(data.message)
        }
      },
      fail: reject
    })
  })
}

// 使用示例
async function chooseAndUploadAvatar() {
  const { tempFilePaths } = await uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera']
  })
  
  const result = await uploadFile(tempFilePaths[0], 'avatar')
  this.formData.avatar = result.fileUrl
}
```

---

## 六、测试用例

### 6.1 关系类型测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 添加父女关系 | father_daughter | 添加成功 |
| 添加配偶关系 | spouse | 添加成功，双向关联 |
| 添加兄弟姐妹关系 | sibling | 添加成功 |

### 6.2 文件上传测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 正常上传 | 上传jpg图片 | 返回文件URL |
| 文件过大 | 上传10MB图片 | 提示文件过大 |
| 非法类型 | 上传exe文件 | 提示文件类型不允许 |
| 无权限上传 | 不带Token | 401未授权 |

---

## 七、交付物

- [ ] RelationType 枚举完善
- [ ] 关系管理接口
- [ ] FileService 接口和实现
- [ ] LocalFileServiceImpl 本地存储实现
- [ ] OssFileServiceImpl 云存储实现
- [ ] FileController 上传接口
- [ ] 静态资源配置
- [ ] 头像上传组件
- [ ] 小程序上传工具
- [ ] 单元测试代码
