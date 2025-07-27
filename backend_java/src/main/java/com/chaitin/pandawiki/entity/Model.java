package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型实体
 */
@Data
@TableName("models")
public class Model {
    
    @TableId
    private String id;
    
    private ModelProvider provider;
    
    private String model;
    
    private String apiKey;
    
    private String apiHeader;
    
    private String baseUrl;
    
    private String apiVersion;
    
    private ModelType type;
    
    private Boolean isActive;
    
    private Long promptTokens;
    
    private Long completionTokens;
    
    private Long totalTokens;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 模型提供商
     */
    public enum ModelProvider {
        OPENAI("OPENAI"),
        OLLAMA("OLLAMA"),
        DEEPSEEK("DEEPSEEK"),
        MOONSHOT("MOONSHOT"),
        SILICON_FLOW("SILICON_FLOW"),
        AZURE_OPENAI("AZURE_OPENAI"),
        BAIZHI_CLOUD("BAIZHI_CLOUD"),
        HUNYUAN("HUNYUAN"),
        BAILIAN("BAILIAN"),
        VOLCENGINE("VOLCENGINE"),
        OTHER("OTHER");
        
        @EnumValue
        private final String value;
        
        ModelProvider(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 模型类型
     */
    public enum ModelType {
        chat("chat"),
        embedding("embedding"),
        rerank("rerank");
        
        @EnumValue
        private final String value;
        
        ModelType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
} 