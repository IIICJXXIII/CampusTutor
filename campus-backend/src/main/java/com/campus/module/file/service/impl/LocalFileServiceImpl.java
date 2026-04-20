package com.campus.module.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.campus.common.exception.BusinessException;
import com.campus.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    /** 允许的文件类型 */
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "image/x-ms-bmp", "image/tiff", "image/svg+xml",
            "application/pdf"
    );

    /** 最大文件大小 (10MB) */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    @Override
    public String upload(MultipartFile file, String folder) {
        // 校验文件
        validateFile(file);

        // 生成文件路径: uploads/{folder}/{yyyyMMdd}/{uuid}.{ext}
        String datePath = DateUtil.format(new Date(), "yyyyMMdd");
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? FileUtil.extName(originalFilename) : "jpg";
        String newFileName = IdUtil.fastSimpleUUID() + "." + ext;

        String relativePath = folder + "/" + datePath + "/" + newFileName;
        String fullPath = uploadPath + "/" + relativePath;

        // 创建目录并保存文件
        try {
            File destFile = new File(fullPath);
            // 确保目录存在
            FileUtil.mkdir(destFile.getParentFile());
            // 使用 getInputStream() + copy 方式替代 transferTo，避免相对路径问题
            FileUtil.writeFromStream(file.getInputStream(), destFile);
            log.info("文件上传成功: {}", fullPath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        // 返回访问 URL
        return baseUrl + "/" + relativePath;
    }

    @Override
    public boolean delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return false;
        }

        String relativePath = fileUrl.replace(baseUrl, "");
        String fullPath = uploadPath + relativePath;

        File file = new File(fullPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("文件删除{}: {}", deleted ? "成功" : "失败", fullPath);
            return deleted;
        }
        return false;
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持图片和PDF");
        }
    }
}
