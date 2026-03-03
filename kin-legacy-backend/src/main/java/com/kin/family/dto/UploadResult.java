package com.kin.family.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileUrl;
    private String thumbnailUrl;
}
