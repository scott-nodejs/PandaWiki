package com.chaitin.pandawiki.service;

import java.io.InputStream;

/**
 * 临时文件存储服务接口
 * 用于将文件上传到可公开访问的存储服务
 * 
 * @author chaitin
 */
public interface TempFileStorageService {
    
    /**
     * 上传文件到临时存储
     * 
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 文件类型
     * @return 可公开访问的文件URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadFile(InputStream inputStream, String fileName, String contentType) throws Exception;
    
    /**
     * 删除临时文件
     * 
     * @param fileUrl 文件URL
     * @throws Exception 删除失败时抛出异常
     */
    void deleteFile(String fileUrl) throws Exception;
    
    /**
     * 检查服务是否可用
     * 
     * @return true表示服务可用
     */
    boolean isAvailable();
} 