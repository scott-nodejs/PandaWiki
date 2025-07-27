package com.chaitin.pandawiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 爬虫服务配置类
 * 
 * @author chaitin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {
    
    /**
     * 爬虫服务地址
     */
    private String serviceUrl;
    
    /**
     * Nginx服务地址，用于处理static-file路径
     */
    private String nginxUrl;
} 