package com.campus.module.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 子目录 (如: avatar, cert, clock-in)
     * @return 文件访问 URL
     */
    String upload(MultipartFile file, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     * @return 是否删除成功
     */
    boolean delete(String fileUrl);
}
