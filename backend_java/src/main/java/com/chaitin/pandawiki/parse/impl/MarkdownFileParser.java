package com.chaitin.pandawiki.parse.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Markdown文件解析器
 * 
 * @author chaitin
 */
public class MarkdownFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("md", "markdown", "mdown", "mkd");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        String content = readInputStream(inputStream);
        
        // Markdown文件本身就是目标格式，只需要清理
        StringBuilder markdown = new StringBuilder();
        
        // 如果内容不是以标题开头，添加文件名作为标题
        String trimmedContent = content.trim();
        if (!trimmedContent.startsWith("#")) {
            String title = fileName.substring(0, fileName.lastIndexOf('.') != -1 
                ? fileName.lastIndexOf('.') : fileName.length());
            markdown.append("# ").append(title).append("\n\n");
        }
        
        markdown.append(content);
        
        return cleanText(markdown.toString());
    }
    
    @Override
    public String getParserName() {
        return "Markdown文件解析器";
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级，因为是原生格式
    }
} 