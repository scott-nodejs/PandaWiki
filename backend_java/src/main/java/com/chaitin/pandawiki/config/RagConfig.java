package com.chaitin.pandawiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagConfig {
    /**
     * 提供商
     */
    private String provider;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;
} 