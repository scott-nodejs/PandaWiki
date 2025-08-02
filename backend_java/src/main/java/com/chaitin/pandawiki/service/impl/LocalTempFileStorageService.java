package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.service.TempFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地临时文件存储服务实现
 * 将文件保存到本地文件系统，并通过HTTP服务提供访问
 *
 * @author chaitin
 */
@Slf4j
@Service
public class LocalTempFileStorageService implements TempFileStorageService {

    @Value("${temp.storage.local.base-path:${java.io.tmpdir}/pandawiki-temp}")
    private String basePath;

    @Value("${temp.storage.local.base-url:http://localhost:8080/temp}")
    private String baseUrl;

    @Value("${temp.storage.local.enabled:true}")
    private boolean enabled;

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType) throws Exception {
        if (!enabled) {
            throw new Exception("本地临时文件存储服务未启用");
        }

        // 确保存储目录存在
        Path storageDir = Paths.get(basePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
            log.info("创建临时存储目录: {}", storageDir);
        }

        // 生成唯一文件名
        String extension = getFileExtension(fileName);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path filePath = storageDir.resolve(uniqueFileName);

        // 保存文件
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }

        // 返回可访问的URL
        String fileUrl = baseUrl + "/" + uniqueFileName;
        log.info("文件上传成功: {} -> {}", fileName, fileUrl);

        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        if (!enabled || fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return;
        }

        // 从URL提取文件名
        String fileName = fileUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(basePath, fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("临时文件已删除: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("删除临时文件失败: {}", filePath, e);
        }
    }

    @Override
    public boolean isAvailable() {

        this.enabled = true;

        if (!enabled) {
            return false;
        }

        try {
            Path storageDir = Paths.get(basePath);
            return Files.isDirectory(storageDir) || Files.isWritable(storageDir.getParent());
        } catch (Exception e) {
            log.warn("检查临时存储服务可用性失败", e);
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
