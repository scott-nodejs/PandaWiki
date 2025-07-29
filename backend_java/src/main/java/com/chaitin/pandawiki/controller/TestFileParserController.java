package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.parse.FileParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * 文件解析器测试控制器
 * 
 * @author chaitin
 */
@Slf4j
@RestController
@RequestMapping("/api/test/parser")
@RequiredArgsConstructor
public class TestFileParserController {
    
    private final FileParserFactory fileParserFactory;
    
    /**
     * 获取支持的文件格式
     * 
     * @return 支持的文件扩展名列表
     */
    @GetMapping("/supported-formats")
    public Set<String> getSupportedFormats() {
        log.info("获取支持的文件格式");
        return fileParserFactory.getSupportedExtensions();
    }
    
    /**
     * 获取所有注册的解析器信息
     * 
     * @return 解析器信息列表
     */
    @GetMapping("/parsers")
    public Object getAllParsers() {
        log.info("获取所有解析器信息");
        return Map.of(
            "parsers", fileParserFactory.getAllParsersInfo(),
            "supportedExtensions", fileParserFactory.getSupportedExtensions()
        );
    }
    
    /**
     * 检查文件是否支持解析
     * 
     * @param fileName 文件名
     * @return 是否支持
     */
    @GetMapping("/check-support")
    public Map<String, Object> checkSupport(@RequestParam String fileName) {
        log.info("检查文件是否支持解析: {}", fileName);
        
        boolean supported = fileParserFactory.isSupported(fileName);
        String parserName = null;
        
        if (supported) {
            var parser = fileParserFactory.getParser(fileName);
            if (parser != null) {
                parserName = parser.getParserName();
            }
        }
        
        return Map.of(
            "fileName", fileName,
            "supported", supported,
            "parserName", parserName != null ? parserName : "无"
        );
    }
    
    /**
     * 上传文件并解析为Markdown
     * 
     * @param file 上传的文件
     * @return 解析结果
     */
    @PostMapping("/parse-file")
    public Map<String, Object> parseFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            log.info("解析上传文件: {}, 大小: {} bytes", fileName, file.getSize());
            
            if (fileName == null || fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("文件名不能为空");
            }
            
            if (!fileParserFactory.isSupported(fileName)) {
                return Map.of(
                    "success", false,
                    "message", "不支持的文件格式: " + fileName,
                    "supportedFormats", fileParserFactory.getSupportedExtensions()
                );
            }
            
            String markdownContent;
            try (InputStream inputStream = file.getInputStream()) {
                markdownContent = fileParserFactory.parseToMarkdown(inputStream, fileName);
            }
            
            return Map.of(
                "success", true,
                "fileName", fileName,
                "fileSize", file.getSize(),
                "markdownContent", markdownContent,
                "contentLength", markdownContent.length()
            );
            
        } catch (Exception e) {
            log.error("文件解析失败: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "message", "文件解析失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 健康检查
     * 
     * @return 状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "message", "文件解析器服务正常运行",
            "registeredParsers", fileParserFactory.getAllParsersInfo().size(),
            "supportedFormats", fileParserFactory.getSupportedExtensions().size()
        );
    }
} 