package com.chaitin.pandawiki.parse.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Word文件解析器
 * 
 * 注意：此解析器需要Apache POI依赖
 * 在pom.xml中添加以下依赖：
 * <dependency>
 *     <groupId>org.apache.poi</groupId>
 *     <artifactId>poi</artifactId>
 *     <version>5.2.3</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.apache.poi</groupId>
 *     <artifactId>poi-ooxml</artifactId>
 *     <version>5.2.3</version>
 * </dependency>
 * <dependency>
 *     <groupId>org.apache.poi</groupId>
 *     <artifactId>poi-scratchpad</artifactId>
 *     <version>5.2.3</version>
 * </dependency>
 * 
 * @author chaitin
 */
@Slf4j
public class WordFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("docx", "doc");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        try {
            // 检测文件格式
            String content;
            if (fileName.toLowerCase().endsWith(".docx")) {
                content = parseDocx(inputStream);
            } else {
                content = parseDoc(inputStream);
            }
            
            markdown.append(textToMarkdown(content, fileName));
            
        } catch (Exception e) {
            log.warn("Word文件解析失败，可能缺少Apache POI依赖: {}", e.getMessage());
            markdown.append("# Word文档解析\n\n");
            markdown.append("> **注意**: Word文档解析失败，可能需要添加Apache POI依赖。\n\n");
            markdown.append("**文件信息**:\n");
            markdown.append("- 文件名: ").append(fileName).append("\n");
            markdown.append("- 格式: Microsoft Word文档\n");
            markdown.append("- 状态: 需要配置解析器依赖\n\n");
            markdown.append("请联系管理员配置Word文档解析功能。");
        }
        
        return cleanText(markdown.toString());
    }
    
    /**
     * 解析DOCX文件（Office 2007+格式）
     */
    private String parseDocx(InputStream inputStream) throws Exception {
        // 使用反射调用Apache POI，避免编译时依赖
        try {
            // XWPFDocument document = new XWPFDocument(inputStream);
            Class<?> documentClass = Class.forName("org.apache.poi.xwpf.usermodel.XWPFDocument");
            Object document = documentClass.getConstructor(InputStream.class).newInstance(inputStream);
            
            // XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            Class<?> extractorClass = Class.forName("org.apache.poi.xwpf.extractor.XWPFWordExtractor");
            Object extractor = extractorClass.getConstructor(documentClass).newInstance(document);
            
            // String text = extractor.getText();
            String text = (String) extractorClass.getMethod("getText").invoke(extractor);
            
            // 关闭资源
            extractorClass.getMethod("close").invoke(extractor);
            documentClass.getMethod("close").invoke(document);
            
            return text;
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Apache POI OOXML library not found", e);
        }
    }
    
    /**
     * 解析DOC文件（Office 97-2003格式）
     */
    private String parseDoc(InputStream inputStream) throws Exception {
        try {
            // HWPFDocument document = new HWPFDocument(inputStream);
            Class<?> documentClass = Class.forName("org.apache.poi.hwpf.HWPFDocument");
            Object document = documentClass.getConstructor(InputStream.class).newInstance(inputStream);
            
            // WordExtractor extractor = new WordExtractor(document);
            Class<?> extractorClass = Class.forName("org.apache.poi.hwpf.extractor.WordExtractor");
            Object extractor = extractorClass.getConstructor(documentClass).newInstance(document);
            
            // String text = extractor.getText();
            String text = (String) extractorClass.getMethod("getText").invoke(extractor);
            
            // 关闭资源
            extractorClass.getMethod("close").invoke(extractor);
            documentClass.getMethod("close").invoke(document);
            
            return text;
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Apache POI HWPF library not found", e);
        }
    }
    
    @Override
    public String getParserName() {
        return "Word文档解析器";
    }
    
    @Override
    public int getPriority() {
        return 70;
    }
} 