package com.chaitin.pandawiki.parse.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * TXT文件解析器
 * 
 * @author chaitin
 */
public class TxtFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("txt", "text", "log");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        String content = readInputStream(inputStream);
        
        // 创建文件元信息
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        // 将文本内容转换为Markdown
        markdown.append(textToMarkdown(content, fileName));
        
        return cleanText(markdown.toString());
    }
    
    @Override
    public String getParserName() {
        return "TXT文件解析器";
    }
    
    @Override
    public int getPriority() {
        return 50;
    }
} 