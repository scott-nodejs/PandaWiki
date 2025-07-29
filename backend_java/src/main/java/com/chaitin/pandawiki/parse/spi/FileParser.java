package com.chaitin.pandawiki.parse.spi;

import java.io.InputStream;
import java.util.List;

/**
 * 文件解析器SPI接口
 * 
 * @author chaitin
 */
public interface FileParser {
    
    /**
     * 获取支持的文件扩展名列表
     * 
     * @return 支持的文件扩展名（不包含点号，如：txt, pdf, docx）
     */
    List<String> getSupportedExtensions();
    
    /**
     * 解析文件内容为Markdown格式
     * 
     * @param inputStream 文件输入流
     * @param fileName 文件名（用于获取扩展名和元信息）
     * @return 解析后的Markdown内容
     * @throws Exception 解析异常
     */
    String parseToMarkdown(InputStream inputStream, String fileName) throws Exception;
    
    /**
     * 获取解析器名称
     * 
     * @return 解析器名称
     */
    String getParserName();
    
    /**
     * 获取解析器优先级（数值越小优先级越高）
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
} 