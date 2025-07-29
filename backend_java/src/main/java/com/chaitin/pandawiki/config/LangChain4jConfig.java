package com.chaitin.pandawiki.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenTokenizer;
import dev.langchain4j.model.Tokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LangChain4j手动配置
 * 替代Spring Boot Starter，避免版本兼容性问题
 * 使用Qwen模型替代OpenAI模型
 *
 * @author chaitin
 */
@Configuration
@Slf4j
public class LangChain4jConfig {

    @Value("${langchain4j.dashscope.chat-model.api-key:your-dashscope-api-key}")
    private String dashscopeApiKey;

    @Value("${langchain4j.dashscope.chat-model.model-name:qwen-plus}")
    private String chatModelName;

    @Value("${langchain4j.dashscope.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.dashscope.embedding-model.model-name:text-embedding-v3}")
    private String embeddingModelName;

    /**
     * 配置Qwen Tokenizer - PersistentChatMemoryStore需要
     */
    @Bean
    @Primary
    public Tokenizer tokenizer() {
        log.info("配置Qwen Tokenizer: model={}, 密钥状态: {}", 
                chatModelName, dashscopeApiKey.startsWith("sk-") ? "有效格式" : "无效格式");
        return new QwenTokenizer(chatModelName, dashscopeApiKey);
    }

    /**
     * 配置Qwen同步聊天模型
     */
    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        log.info("配置Qwen聊天模型: model={}, 密钥状态: {}", 
                chatModelName, dashscopeApiKey.startsWith("sk-") ? "有效格式" : "无效格式");
        return QwenChatModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(chatModelName)
                .temperature(temperature.floatValue())
                .build();
    }

    /**
     * 配置Qwen流式聊天模型
     */
    @Bean
    @Primary
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("配置Qwen流式聊天模型: model={}, 密钥状态: {}", 
                chatModelName, dashscopeApiKey.startsWith("sk-") ? "有效格式" : "无效格式");
        return QwenStreamingChatModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(chatModelName)
                .temperature(temperature.floatValue())
                .build();
    }

    /**
     * 配置Qwen3 EmbeddingModel - ContentRetrieverFactory需要
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        log.info("配置Qwen3嵌入模型: model={}, 密钥状态: {}", 
                embeddingModelName, dashscopeApiKey.startsWith("sk-") ? "有效格式" : "无效格式");
        return QwenEmbeddingModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(embeddingModelName)
                .build();
    }
} 