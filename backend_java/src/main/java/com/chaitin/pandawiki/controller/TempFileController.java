package com.chaitin.pandawiki.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 临时文件访问控制器
 * 提供临时文件的HTTP访问接口
 * 
 * @author chaitin
 */
@Slf4j
@RestController
@RequestMapping("/temp")
public class TempFileController {
    
    @Value("${temp.storage.local.base-path:${java.io.tmpdir}/pandawiki-temp}")
    private String basePath;
    
    @Value("${temp.storage.local.enabled:true}")
    private boolean enabled;
    
    /**
     * 获取临时文件
     * 
     * @param fileName 文件名
     * @return 文件内容
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getTempFile(@PathVariable String fileName) {
        if (!enabled) {
            log.warn("临时文件服务未启用");
            return ResponseEntity.notFound().build();
        }
        
        try {
            // 安全检查：防止路径遍历攻击
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.warn("非法文件名访问尝试: {}", fileName);
                return ResponseEntity.badRequest().build();
            }
            
            Path filePath = Paths.get(basePath, fileName);
            File file = filePath.toFile();
            
            if (!file.exists() || !file.isFile()) {
                log.warn("请求的临时文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否在允许的目录内
            Path normalizedBasePath = Paths.get(basePath).normalize();
            Path normalizedFilePath = filePath.normalize();
            if (!normalizedFilePath.startsWith(normalizedBasePath)) {
                log.warn("文件访问路径越界: {}", filePath);
                return ResponseEntity.badRequest().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 确定内容类型
            String contentType = determineContentType(fileName);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            log.debug("提供临时文件访问: {}", filePath);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("访问临时文件失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 检查临时文件服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        if (!enabled) {
            return ResponseEntity.ok("disabled");
        }
        
        try {
            Path storageDir = Paths.get(basePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            return ResponseEntity.ok("healthy");
        } catch (Exception e) {
            log.error("临时文件服务健康检查失败", e);
            return ResponseEntity.internalServerError().body("unhealthy");
        }
    }
    
    /**
     * 确定文件内容类型
     */
    private String determineContentType(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "txt":
                return "text/plain";
            case "md":
                return "text/markdown";
            default:
                return "application/octet-stream";
        }
    }
} 