package com.kin.family.service;

import com.kin.family.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    UploadResult upload(MultipartFile file, String type);

    void delete(String fileId);

    String getAccessUrl(String fileId);
}
