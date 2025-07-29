package com.chaitin.pandawiki.parse.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * PDF文件解析器
 * 
 * 注意：此解析器需要Apache PDFBox依赖
 * 在pom.xml中添加以下依赖：
 * <dependency>
 *     <groupId>org.apache.pdfbox</groupId>
 *     <artifactId>pdfbox</artifactId>
 *     <version>2.0.29</version>
 * </dependency>
 * 
 * @author chaitin
 */
@Slf4j
public class PdfFileParser extends AbstractFileParser {
    
    @Override
    public List<String> getSupportedExtensions() {
        return Collections.singletonList("pdf");
    }
    
    @Override
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        StringBuilder markdown = new StringBuilder();
        markdown.append(createFileMetadata(fileName, null));
        
        try {
            String content = parsePdf(inputStream);
            markdown.append(content);
            
        } catch (Exception e) {
            log.warn("PDF文件解析失败，可能缺少Apache PDFBox依赖: {}", e.getMessage());
            markdown.append("# PDF文档解析\n\n");
            markdown.append("> **注意**: PDF文档解析失败，可能需要添加Apache PDFBox依赖。\n\n");
            markdown.append("**文件信息**:\n");
            markdown.append("- 文件名: ").append(fileName).append("\n");
            markdown.append("- 格式: PDF文档\n");
            markdown.append("- 状态: 需要配置解析器依赖\n\n");
            markdown.append("请联系管理员配置PDF文档解析功能。\n\n");
            markdown.append("**需要的依赖**:\n");
            markdown.append("```xml\n");
            markdown.append("<dependency>\n");
            markdown.append("    <groupId>org.apache.pdfbox</groupId>\n");
            markdown.append("    <artifactId>pdfbox</artifactId>\n");
            markdown.append("    <version>2.0.29</version>\n");
            markdown.append("</dependency>\n");
            markdown.append("```");
        }
        
        return cleanText(markdown.toString());
    }
    
    /**
     * 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) throws Exception {
        try {
            // 临时设置PDFBox日志级别，减少字体警告噪音
            String originalLogLevel = System.getProperty("org.apache.commons.logging.Log");
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            
            try {
                return parsePdfInternal(inputStream);
            } finally {
                // 恢复原始日志设置
                if (originalLogLevel != null) {
                    System.setProperty("org.apache.commons.logging.Log", originalLogLevel);
                } else {
                    System.clearProperty("org.apache.commons.logging.Log");
                }
            }
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Apache PDFBox library not found", e);
        }
    }
    
    /**
     * 内部PDF解析实现
     */
    private String parsePdfInternal(InputStream inputStream) throws Exception {
        // PDDocument document = PDDocument.load(inputStream);
        Class<?> documentClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Object document = documentClass.getMethod("load", InputStream.class).invoke(null, inputStream);
        
        // 获取页数信息
        int pageCount = (Integer) documentClass.getMethod("getNumberOfPages").invoke(document);
        log.info("PDF文档总页数: {}", pageCount);
        
        StringBuilder result = new StringBuilder();
        
        // 添加文档标题
        String fileName = "PDF文档";
        result.append("# ").append(fileName).append("\n\n");
        result.append("**PDF信息**: 共 ").append(pageCount).append(" 页\n\n");
        result.append("---\n\n");
        
        // 创建PDFTextStripper并配置
        Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
        Object stripper = stripperClass.newInstance();
        
        // 设置排序模式，确保文本按正确顺序提取
        stripperClass.getMethod("setSortByPosition", boolean.class).invoke(stripper, true);
        
        // 设置段落结束符
        stripperClass.getMethod("setParagraphEnd", String.class).invoke(stripper, "\n\n");
        stripperClass.getMethod("setWordSeparator", String.class).invoke(stripper, " ");
        stripperClass.getMethod("setLineSeparator", String.class).invoke(stripper, "\n");
        
        // 对于多页PDF，直接使用逐页提取策略确保完整性
        if (pageCount > 1) {
            log.info("检测到多页PDF文档，使用逐页提取策略确保完整性");
            result.append(extractPageByPage(document, stripperClass, pageCount));
        } else {
            // 单页PDF使用一次性提取
            log.info("单页PDF文档，使用一次性提取");
            try {
                stripperClass.getMethod("setStartPage", int.class).invoke(stripper, 1);
                stripperClass.getMethod("setEndPage", int.class).invoke(stripper, 1);
                
                String text = (String) stripperClass.getMethod("getText", documentClass).invoke(stripper, document);
                
                if (text != null && !text.trim().isEmpty()) {
                    log.info("成功提取单页PDF内容，长度: {}", text.length());
                    result.append(formatPdfText(text));
                } else {
                    result.append("*此PDF文档没有可提取的文本内容*\n");
                }
            } catch (Exception e) {
                log.error("单页PDF提取失败: {}", e.getMessage());
                result.append("*PDF内容提取失败: ").append(e.getMessage()).append("*\n");
            }
        }
        
        // 关闭文档
        documentClass.getMethod("close").invoke(document);
        
        return result.toString();
    }
    
    /**
     * 逐页提取PDF内容
     */
    private String extractPageByPage(Object document, Class<?> stripperClass, int pageCount) throws Exception {
        StringBuilder content = new StringBuilder();
        int successfulPages = 0;
        int totalContentLength = 0;
        
        log.info("开始逐页提取PDF内容，总页数: {}", pageCount);
        
        for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
            try {
                log.debug("提取PDF第{}页内容", pageNum);
                
                // 创建新的stripper实例用于每页
                Object pageStripper = stripperClass.newInstance();
                stripperClass.getMethod("setSortByPosition", boolean.class).invoke(pageStripper, true);
                stripperClass.getMethod("setStartPage", int.class).invoke(pageStripper, pageNum);
                stripperClass.getMethod("setEndPage", int.class).invoke(pageStripper, pageNum);
                
                // 设置分隔符
                stripperClass.getMethod("setParagraphEnd", String.class).invoke(pageStripper, "\n\n");
                stripperClass.getMethod("setWordSeparator", String.class).invoke(pageStripper, " ");
                stripperClass.getMethod("setLineSeparator", String.class).invoke(pageStripper, "\n");
                
                String pageText = (String) stripperClass.getMethod("getText", document.getClass())
                    .invoke(pageStripper, document);
                
                if (pageText != null && !pageText.trim().isEmpty()) {
                    content.append("## 第 ").append(pageNum).append(" 页\n\n");
                    String formattedText = formatPdfText(pageText);
                    content.append(formattedText);
                    content.append("\n\n---\n\n");
                    
                    successfulPages++;
                    totalContentLength += pageText.length();
                    log.info("第{}页提取完成，原始内容长度: {}，格式化后长度: {}", 
                        pageNum, pageText.length(), formattedText.length());
                } else {
                    content.append("## 第 ").append(pageNum).append(" 页\n\n");
                    content.append("*此页面没有可提取的文本内容*\n\n---\n\n");
                    log.warn("第{}页无文本内容或提取为空", pageNum);
                }
                
            } catch (Exception e) {
                log.error("提取第{}页失败: {}", pageNum, e.getMessage());
                content.append("## 第 ").append(pageNum).append(" 页\n\n");
                content.append("*页面提取失败: ").append(e.getMessage()).append("*\n\n---\n\n");
            }
        }
        
        log.info("PDF逐页提取完成 - 总页数: {}, 成功提取: {}, 总内容长度: {}", 
            pageCount, successfulPages, totalContentLength);
        
        if (successfulPages == 0) {
            content.append("*整个PDF文档都没有可提取的文本内容，可能是扫描版PDF*\n");
        }
        
        return content.toString();
    }
    
    /**
     * 格式化PDF文本
     */
    private String formatPdfText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // 基础文本清理
        String cleaned = basicTextCleanup(text);
        
        // 分行处理，采用保守策略
        String[] lines = cleaned.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                // 保留适当的空行
                if (!result.toString().endsWith("\n\n")) {
                    result.append("\n");
                }
                continue;
            }
            
            // 简单的格式识别
            if (isSimpleTitle(line)) {
                result.append("## ").append(line).append("\n\n");
            } else if (isSimpleList(line)) {
                result.append("- ").append(cleanListText(line)).append("\n");
            } else {
                // 普通文本，保持原样但清理
                result.append(line);
                
                // 判断是否需要换行
                if (i < lines.length - 1) {
                    String nextLine = lines[i + 1].trim();
                    if (nextLine.isEmpty() || isSimpleTitle(nextLine) || isSimpleList(nextLine)) {
                        result.append("\n\n");
                    } else if (line.matches(".*[.。!！?？]$")) {
                        result.append("\n\n");
                    } else {
                        result.append(" ");
                    }
                }
            }
        }
        
        return cleanText(result.toString());
    }
    
    /**
     * 基础文本清理
     */
    private String basicTextCleanup(String text) {
        if (text == null) return "";
        
        String processed = text;
        
        // 统一换行符
        processed = processed.replaceAll("\\r\\n", "\n");
        processed = processed.replaceAll("\\r", "\n");
        
        // 清理明显的页眉页脚
        processed = processed.replaceAll("(?m)^\\s*第\\s*\\d+\\s*页\\s*$", "");
        processed = processed.replaceAll("(?m)^\\s*Page\\s*\\d+\\s*$", "");
        processed = processed.replaceAll("(?m)^\\s*- \\d+ -\\s*$", "");
        
        // 处理PDF中常见的断行问题（保守处理）
        processed = processed.replaceAll("([\\u4e00-\\u9fa5])\\n([\\u4e00-\\u9fa5])", "$1$2");
        processed = processed.replaceAll("([a-z])\\n([a-z])", "$1 $2");
        
        // 清理多余空格，但保持基本结构
        processed = processed.replaceAll("[ \\t]+", " ");
        processed = processed.replaceAll("\\n{4,}", "\n\n\n");
        
        return processed;
    }
    
    /**
     * 简单的标题识别（更保守）
     */
    private boolean isSimpleTitle(String line) {
        if (line == null || line.length() > 80 || line.length() < 3) {
            return false;
        }
        
        // 明显的数字标题
        if (line.matches("^\\d+[\\.、]\\s*.+") || 
            line.matches("^[一二三四五六七八九十]+[、.]\\s*.+")) {
            return true;
        }
        
        // 全大写的短文本
        if (line.matches("^[A-Z\\s\\d]{3,30}$")) {
            return true;
        }
        
        // 简单的章节标识
        if (line.matches("^(第.+章|第.+节|Chapter|Section).*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 简单的列表识别（更保守）
     */
    private boolean isSimpleList(String line) {
        if (line == null) return false;
        
        // 明显的列表标识符
        return line.matches("^[\\s]*[\\d]+[.。)）]\\s*.+") ||
               line.matches("^[\\s]*[-*•]\\s*.+") ||
               line.matches("^[\\s]*[一二三四五六七八九十]+[、.]\\s*.+") ||
               line.matches("^[\\s]*\\([\\d]+\\)\\s*.+") ||
               line.matches("^[\\s]*（[\\d]+）\\s*.+");
    }
    
    /**
     * 清理列表文本
     */
    private String cleanListText(String line) {
        if (line == null) return "";
        return line.replaceFirst("^[\\s]*[\\d\\-*•一二三四五六七八九十()（）.。、]+[\\s]*", "").trim();
    }
    
    @Override
    public String getParserName() {
        return "PDF文档解析器";
    }
    
    @Override
    public int getPriority() {
        return 90;
    }
} 