package com.campus.module.file.controller;

import com.campus.common.result.Result;
import com.campus.module.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件", description = "上传图片或PDF文件，返回访问URL")
    @PostMapping("/upload")
    public Result<String> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "目录: avatar/cert/clock-in 等") @RequestParam(defaultValue = "common") String folder) {
        String url = fileService.upload(file, folder);
        return Result.success("上传成功", url);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result<Boolean> delete(@Parameter(description = "文件URL") @RequestParam String fileUrl) {
        boolean result = fileService.delete(fileUrl);
        return Result.success(result);
    }
}
