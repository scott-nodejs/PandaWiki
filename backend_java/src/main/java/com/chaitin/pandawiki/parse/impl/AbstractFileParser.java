package com.chaitin.pandawiki.parse.impl;

import com.chaitin.pandawiki.parse.spi.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 抽象文件解析器基类
 * 提供公共的工具方法
 * 
 * @author chaitin
 */
@Slf4j
public abstract class AbstractFileParser implements FileParser {
    
    /**
     * 将输入流读取为字符串
     * 
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IO异常
     */
    protected String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * 清理和格式化文本内容
     * 
     * @param text 原始文本
     * @return 清理后的文本
     */
    protected String cleanText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        
        // 移除多余的空白行
        String cleaned = text.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        
        // 修复常见的编码问题
        cleaned = cleaned.replace("\uFEFF", ""); // 移除BOM
        
        return cleaned.trim();
    }
    
    /**
     * 将普通文本转换为Markdown格式
     * 
     * @param text 普通文本
     * @param fileName 文件名（用于标题）
     * @return Markdown格式文本
     */
    protected String textToMarkdown(String text, String fileName) {
        StringBuilder markdown = new StringBuilder();
        
        // 添加文件标题
        if (StringUtils.hasText(fileName)) {
            String title = fileName.substring(0, fileName.lastIndexOf('.') != -1 
                ? fileName.lastIndexOf('.') : fileName.length());
            markdown.append("# ").append(title).append("\n\n");
        }
        
        // 处理文本内容
        String[] lines = text.split("\n");
        boolean inCodeBlock = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // 检测可能的代码块
            if (trimmedLine.matches(".*\\{.*\\}.*") || 
                trimmedLine.matches(".*<.*>.*") ||
                trimmedLine.matches("^\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(.*\\).*")) {
                
                if (!inCodeBlock) {
                    markdown.append("```\n");
                    inCodeBlock = true;
                }
                markdown.append(line).append("\n");
            } else {
                if (inCodeBlock) {
                    markdown.append("```\n\n");
                    inCodeBlock = false;
                }
                
                // 检测标题（全大写或以数字开头的行）
                if (trimmedLine.matches("^[A-Z\\s]+$") && trimmedLine.length() > 2) {
                    markdown.append("## ").append(trimmedLine).append("\n\n");
                } else if (trimmedLine.matches("^\\d+\\..*")) {
                    markdown.append("### ").append(trimmedLine).append("\n\n");
                } else if (StringUtils.hasText(trimmedLine)) {
                    markdown.append(line).append("\n");
                } else {
                    markdown.append("\n");
                }
            }
        }
        
        // 关闭可能未关闭的代码块
        if (inCodeBlock) {
            markdown.append("```\n");
        }
        
        return cleanText(markdown.toString());
    }
    
    /**
     * 转义Markdown特殊字符
     * 
     * @param text 原始文本
     * @return 转义后的文本
     */
    protected String escapeMarkdown(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        
        return text
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
    
    /**
     * 添加文件元信息到Markdown开头
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小（可选）
     * @return 元信息Markdown
     */
    protected String createFileMetadata(String fileName, Long fileSize) {
        StringBuilder metadata = new StringBuilder();
        
        metadata.append("---\n");
        metadata.append("文件名: ").append(fileName).append("\n");
        metadata.append("解析时间: ").append(java.time.LocalDateTime.now()).append("\n");
        if (fileSize != null) {
            metadata.append("文件大小: ").append(formatFileSize(fileSize)).append("\n");
        }
        metadata.append("---\n\n");
        
        return metadata.toString();
    }
    
    /**
     * 格式化文件大小
     * 
     * @param bytes 字节数
     * @return 格式化后的大小字符串
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
} 