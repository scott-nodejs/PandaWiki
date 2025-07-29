package com.chaitin.pandawiki.parse;

import com.chaitin.pandawiki.parse.spi.FileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件解析器工厂类
 * 使用SPI机制自动发现和管理文件解析器
 * 
 * @author chaitin
 */
@Slf4j
@Component
public class FileParserFactory {
    
    /**
     * 扩展名到解析器的映射
     * key: 文件扩展名（小写，不含点号）
     * value: 对应的解析器实例
     */
    private final Map<String, FileParser> extensionParserMap = new ConcurrentHashMap<>();
    
    /**
     * 所有已注册的解析器
     */
    private final List<FileParser> allParsers = new ArrayList<>();
    
    /**
     * 初始化方法，自动发现并注册所有解析器
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化文件解析器...");
        
        // 使用SPI机制加载所有FileParser实现
        ServiceLoader<FileParser> serviceLoader = ServiceLoader.load(FileParser.class);
        
        for (FileParser parser : serviceLoader) {
            registerParser(parser);
        }
        
        log.info("文件解析器初始化完成，共注册{}个解析器", allParsers.size());
        logRegisteredParsers();
    }
    
    /**
     * 注册解析器
     * 
     * @param parser 解析器实例
     */
    public void registerParser(FileParser parser) {
        if (parser == null) {
            log.warn("尝试注册空的解析器，已忽略");
            return;
        }
        
        try {
            allParsers.add(parser);
            
            // 为每个支持的扩展名注册解析器
            for (String extension : parser.getSupportedExtensions()) {
                String normalizedExt = extension.toLowerCase().trim();
                
                FileParser existingParser = extensionParserMap.get(normalizedExt);
                if (existingParser != null) {
                    // 如果已存在解析器，比较优先级
                    if (parser.getPriority() < existingParser.getPriority()) {
                        extensionParserMap.put(normalizedExt, parser);
                        log.info("解析器{}替换了{}，支持扩展名: {}", 
                            parser.getParserName(), existingParser.getParserName(), normalizedExt);
                    } else {
                        log.debug("解析器{}优先级较低，未替换现有解析器{}，扩展名: {}", 
                            parser.getParserName(), existingParser.getParserName(), normalizedExt);
                    }
                } else {
                    extensionParserMap.put(normalizedExt, parser);
                    log.debug("注册解析器{}，支持扩展名: {}", parser.getParserName(), normalizedExt);
                }
            }
            
        } catch (Exception e) {
            log.error("注册解析器{}失败: {}", parser.getParserName(), e.getMessage(), e);
        }
    }
    
    /**
     * 根据文件名获取对应的解析器
     * 
     * @param fileName 文件名
     * @return 对应的解析器，如果没有找到则返回null
     */
    public FileParser getParser(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return null;
        }
        
        return extensionParserMap.get(extension.toLowerCase());
    }
    
    /**
     * 检查是否支持指定的文件格式
     * 
     * @param fileName 文件名
     * @return true如果支持，false如果不支持
     */
    public boolean isSupported(String fileName) {
        return getParser(fileName) != null;
    }
    
    /**
     * 解析文件内容为Markdown格式
     * 
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @return 解析后的Markdown内容
     * @throws Exception 解析异常
     */
    public String parseToMarkdown(InputStream inputStream, String fileName) throws Exception {
        FileParser parser = getParser(fileName);
        if (parser == null) {
            throw new UnsupportedOperationException("不支持的文件格式: " + fileName);
        }
        
        log.info("使用解析器{}解析文件: {}", parser.getParserName(), fileName);
        
        try {
            String markdown = parser.parseToMarkdown(inputStream, fileName);
            log.debug("文件解析完成: {}, 内容长度: {}", fileName, 
                markdown != null ? markdown.length() : 0);
            return markdown;
        } catch (Exception e) {
            log.error("解析文件{}失败: {}", fileName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取所有支持的文件扩展名
     * 
     * @return 支持的文件扩展名列表
     */
    public Set<String> getSupportedExtensions() {
        return new HashSet<>(extensionParserMap.keySet());
    }
    
    /**
     * 获取所有已注册的解析器信息
     * 
     * @return 解析器信息列表
     */
    public List<ParserInfo> getAllParsersInfo() {
        List<ParserInfo> infos = new ArrayList<>();
        for (FileParser parser : allParsers) {
            ParserInfo info = new ParserInfo();
            info.setName(parser.getParserName());
            info.setPriority(parser.getPriority());
            info.setSupportedExtensions(new ArrayList<>(parser.getSupportedExtensions()));
            infos.add(info);
        }
        return infos;
    }
    
    /**
     * 从文件名中提取扩展名
     * 
     * @param fileName 文件名
     * @return 扩展名（不含点号），如果没有扩展名则返回null
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
    
    /**
     * 记录已注册的解析器信息
     */
    private void logRegisteredParsers() {
        if (log.isInfoEnabled()) {
            log.info("已注册的文件解析器:");
            for (FileParser parser : allParsers) {
                log.info("- {}: {} (优先级: {})", 
                    parser.getParserName(), 
                    parser.getSupportedExtensions(), 
                    parser.getPriority());
            }
            log.info("支持的文件扩展名: {}", getSupportedExtensions());
        }
    }
    
    /**
     * 解析器信息
     */
    public static class ParserInfo {
        private String name;
        private int priority;
        private List<String> supportedExtensions;
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        
        public List<String> getSupportedExtensions() { return supportedExtensions; }
        public void setSupportedExtensions(List<String> supportedExtensions) { 
            this.supportedExtensions = supportedExtensions; 
        }
    }
} 