package com.chaitin.pandawiki.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 上传文件到指定知识库
     *
     * @param file 文件
     * @param kbId 知识库ID
     * @return 文件key
     */
    String uploadFile(MultipartFile file, String kbId);
    
    /**
     * 上传文件流
     *
     * @param inputStream 文件流
     * @param fileName    文件名
     * @param contentType 文件类型
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType);
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 获取文件访问URL
     *
     * @param fileName 文件名
     * @return 文件访问URL
     */
    String getFileUrl(String fileName);
} 