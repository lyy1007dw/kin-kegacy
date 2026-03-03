package com.kin.family.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.kin.family.dto.UploadResult;
import com.kin.family.exception.BusinessException;
import com.kin.family.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {

    private final OSS ossClient;

    @Value("${file.oss.bucket-name}")
    private String bucketName;

    @Value("${file.oss.endpoint}")
    private String endpoint;

    @Value("${file.oss.url-prefix}")
    private String urlPrefix;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    @Value("${file.upload.max-size:5242880}")
    private Long maxSize;

    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public UploadResult upload(MultipartFile file, String type) {
        // 校验文件
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);

        // 生成文件ID
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String objectName = String.format("upload/%s/%s/%s.%s",
                type,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")),
                fileId,
                ext);

        try {
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 上传到OSS
            ossClient.putObject(bucketName, objectName, file.getInputStream(), metadata);

            String fileUrl = urlPrefix + objectName;

            return UploadResult.builder()
                    .fileId(fileId)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .fileUrl(fileUrl)
                    .thumbnailUrl(fileUrl)
                    .build();

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void delete(String fileId) {
        // 根据fileId查询并删除文件（简化处理：需要记录fileId与objectName的映射）
        // 这里可以先实现根据前缀删除
        throw new UnsupportedOperationException("OSS删除需要配合数据库记录");
    }

    @Override
    public String getAccessUrl(String fileId) {
        // 返回公开访问URL或生成签名URL
        throw new UnsupportedOperationException("需要配合数据库记录");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        String ext = getFileExtension(file.getOriginalFilename());
        if (!allowedTypes.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件类型");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
