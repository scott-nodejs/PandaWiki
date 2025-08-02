package com.chaitin.pandawiki.config;

import com.chaitin.pandawiki.parse.FileParserFactory;
import com.chaitin.pandawiki.parse.impl.MinerUPdfParser;
import com.chaitin.pandawiki.service.TempFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * MinerU 配置验证类
 * 在应用启动时验证 MinerU 相关配置和服务状态
 * 
 * @author chaitin
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "mineru.api.enabled", havingValue = "true", matchIfMissing = true)
public class MinerUConfiguration implements CommandLineRunner {
    
    @Value("${mineru.api.token:}")
    private String mineruToken;
    
    @Value("${mineru.api.base-url:https://mineru.net}")
    private String mineruBaseUrl;
    
    @Value("${mineru.api.enabled:true}")
    private boolean mineruEnabled;
    
    @Autowired
    private FileParserFactory fileParserFactory;
    
    @Autowired
    private TempFileStorageService tempFileStorageService;
    
    @Autowired(required = false)
    private MinerUPdfParser minerUPdfParser;
    
    @Override
    public void run(String... args) {
        log.info("=== MinerU 配置验证 ===");
        
        // 1. 检查基础配置
        validateBasicConfiguration();
        
        // 2. 检查解析器注册
        validateParserRegistration();
        
        // 3. 检查临时存储服务
        validateTempStorageService();
        
        // 4. 显示配置摘要
        displayConfigurationSummary();
        
        log.info("=== MinerU 配置验证完成 ===");
    }
    
    /**
     * 验证基础配置
     */
    private void validateBasicConfiguration() {
        log.info("检查 MinerU 基础配置...");
        
        if (!mineruEnabled) {
            log.warn("⚠️  MinerU 解析器已禁用");
            return;
        }
        
        if (mineruToken == null || mineruToken.trim().isEmpty()) {
            log.warn("⚠️  MinerU API Token 未配置，将使用回退解析器");
            log.info("💡 请配置环境变量: MINERU_API_TOKEN=your-token");
        } else {
            String maskedToken = maskToken(mineruToken);
            log.info("✅ MinerU API Token 已配置: {}", maskedToken);
        }
        
        log.info("📡 MinerU API 地址: {}", mineruBaseUrl);
    }
    
    /**
     * 验证解析器注册
     */
    private void validateParserRegistration() {
        log.info("检查 PDF 解析器注册状态...");
        
        // 检查 MinerUPdfParser Bean 是否存在
        if (minerUPdfParser == null) {
            log.error("❌ MinerUPdfParser Bean 未找到，请检查 Spring 组件扫描配置");
            return;
        }
        
        log.info("✅ MinerUPdfParser Bean 已注册");
        
        // 检查 SPI 注册
        boolean minerURegistered = fileParserFactory.isSupported("test.pdf");
        if (minerURegistered) {
            log.info("✅ PDF 解析器已在 SPI 中注册");
            
            // 获取 PDF 解析器并检查类型
            try {
                var parser = fileParserFactory.getParser("test.pdf");
                log.info("📋 当前 PDF 解析器: {} (优先级: {})", 
                    parser.getParserName(), parser.getPriority());
                
                if (parser instanceof MinerUPdfParser) {
                    log.info("🎯 使用 MinerU 解析器 (高质量解析)");
                } else {
                    log.info("📄 使用传统解析器 (回退方案)");
                }
            } catch (Exception e) {
                log.error("❌ 获取 PDF 解析器失败: {}", e.getMessage());
            }
        } else {
            log.error("❌ PDF 解析器未在 SPI 中注册");
        }
    }
    
    /**
     * 验证临时存储服务
     */
    private void validateTempStorageService() {
        log.info("检查临时文件存储服务...");
        
        if (tempFileStorageService.isAvailable()) {
            log.info("✅ 临时文件存储服务可用");
        } else {
            log.warn("⚠️  临时文件存储服务不可用，MinerU 将无法工作");
            log.info("💡 请检查存储路径配置和权限");
        }
    }
    
    /**
     * 显示配置摘要
     */
    private void displayConfigurationSummary() {
        log.info("📊 MinerU 配置摘要:");
        log.info("  🔘 服务状态: {}", mineruEnabled ? "已启用" : "已禁用");
        log.info("  🔘 API Token: {}", 
            (mineruToken != null && !mineruToken.trim().isEmpty()) ? "已配置" : "未配置");
        log.info("  🔘 临时存储: {}", 
            tempFileStorageService.isAvailable() ? "可用" : "不可用");
        log.info("  🔘 解析器Bean: {}", 
            minerUPdfParser != null ? "已注册" : "未找到");
        
        // 综合状态判断
        boolean fullyOperational = mineruEnabled && 
            mineruToken != null && !mineruToken.trim().isEmpty() &&
            tempFileStorageService.isAvailable() && 
            minerUPdfParser != null;
            
        if (fullyOperational) {
            log.info("🎉 MinerU 解析器完全可用！");
        } else {
            log.warn("⚠️  MinerU 解析器部分功能受限，将使用回退解析器");
        }
    }
    
    /**
     * 隐藏 Token 敏感信息
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
} 