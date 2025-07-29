package com.chaitin.pandawiki.parse.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * PowerPoint文件解析器
 * 
 * @author chaitin
 */
@Slf4j
public class PowerPointFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("pptx", "ppt");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        try {
            String content = parsePowerPoint(inputStream, fileName);
            markdown.append("# ").append(getFileNameWithoutExtension(fileName)).append("\n\n");
            markdown.append(content);
            
        } catch (Exception e) {
            log.warn("PowerPoint文件解析失败: {}", e.getMessage());
            markdown.append("# PowerPoint文档解析\n\n");
            markdown.append("> **注意**: PowerPoint文档解析失败，可能需要添加Apache POI依赖。\n\n");
            markdown.append("**文件信息**:\n");
            markdown.append("- 文件名: ").append(fileName).append("\n");
            markdown.append("- 格式: Microsoft PowerPoint文档\n");
            markdown.append("- 状态: 需要配置解析器依赖\n\n");
        }
        
        return cleanText(markdown.toString());
    }
    
    private String parsePowerPoint(InputStream inputStream, String fileName) throws Exception {
        // 简化实现，显示需要依赖的信息
        return "**PowerPoint文档**\n\n需要Apache POI依赖来解析此文件类型。\n\n" +
               "请在pom.xml中添加POI依赖以支持PowerPoint文档解析。";
    }
    
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
    
    @Override
    public String getParserName() {
        return "PowerPoint文档解析器";
    }
    
    @Override
    public int getPriority() {
        return 85;
    }
} 