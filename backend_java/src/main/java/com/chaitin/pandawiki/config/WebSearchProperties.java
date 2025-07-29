package com.chaitin.pandawiki.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: iohw
 * @date: 2025/4/14 23:02
 * @description:
 */
@Configuration
@ConfigurationProperties(prefix = "search")
@Data
public class WebSearchProperties {
    private String engine;

    private String apiKey;
}
