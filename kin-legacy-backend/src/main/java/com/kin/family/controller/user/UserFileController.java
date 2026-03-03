package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.Result;
import com.kin.family.dto.UploadResult;
import com.kin.family.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class UserFileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @RequireLogin
    @OperationLogger(module = "文件管理", operation = "上传文件")
    public Result<UploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {
        UploadResult result = fileService.upload(file, type);
        return Result.success(result);
    }

    @DeleteMapping("/{fileId}")
    @RequireLogin
    @OperationLogger(module = "文件管理", operation = "删除文件")
    public Result<Void> delete(@PathVariable String fileId) {
        fileService.delete(fileId);
        return Result.success();
    }
}
