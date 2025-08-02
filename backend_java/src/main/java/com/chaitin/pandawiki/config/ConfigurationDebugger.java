package com.chaitin.pandawiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 配置调试器
 * 在应用启动时显示所有相关配置信息，帮助诊断配置问题
 * 
 * @author chaitin
 */
@Slf4j
@Component
public class ConfigurationDebugger implements CommandLineRunner {
    
    @Autowired
    private Environment environment;
    
    @Override
    public void run(String... args) {
        log.info("==================== 配置调试信息 ====================");
        
        // 1. 显示激活的 Profiles
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        log.info("🔘 激活的 Profiles: {}", Arrays.toString(activeProfiles));
        log.info("🔘 默认 Profiles: {}", Arrays.toString(defaultProfiles));
        
        // 2. 显示 MinerU 相关配置
        log.info("📋 MinerU 配置详情:");
        logProperty("mineru.api.enabled", "mineru.api.enabled");
        logProperty("mineru.api.base-url", "mineru.api.base-url");
        logProperty("mineru.api.token", "mineru.api.token", true); // 敏感信息
        logProperty("mineru.api.timeout", "mineru.api.timeout");
        logProperty("mineru.api.poll-interval", "mineru.api.poll-interval");
        logProperty("mineru.api.is-ocr", "mineru.api.is-ocr");
        logProperty("mineru.api.enable-formula", "mineru.api.enable-formula");
        logProperty("mineru.api.enable-table", "mineru.api.enable-table");
        logProperty("mineru.api.language", "mineru.api.language");
        logProperty("mineru.api.model-version", "mineru.api.model-version");
        
        // 3. 检查配置文件加载情况
        log.info("📄 配置源信息:");
        String configLocation = environment.getProperty("spring.config.location");
        String configName = environment.getProperty("spring.config.name");
        log.info("  - config.location: {}", configLocation != null ? configLocation : "默认");
        log.info("  - config.name: {}", configName != null ? configName : "application");
        
        log.info("=====================================================");
    }
    
    /**
     * 记录配置属性值
     */
    private void logProperty(String displayName, String propertyKey) {
        logProperty(displayName, propertyKey, false);
    }
    
    /**
     * 记录配置属性值（可选择是否隐藏敏感信息）
     */
    private void logProperty(String displayName, String propertyKey, boolean sensitive) {
        String value = environment.getProperty(propertyKey);
        if (value == null) {
            log.info("  ❌ {}: <未配置>", displayName);
        } else {
            if (sensitive && value.length() > 8) {
                String maskedValue = value.substring(0, 4) + "****" + value.substring(value.length() - 4);
                log.info("  ✅ {}: {} (长度: {})", displayName, maskedValue, value.length());
            } else {
                log.info("  ✅ {}: {}", displayName, value);
            }
        }
    }
} 