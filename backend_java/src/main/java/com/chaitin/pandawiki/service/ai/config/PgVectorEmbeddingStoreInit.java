package com.chaitin.pandawiki.service.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入存储配置
 * 使用内存存储作为临时解决方案，避免PostgreSQL依赖
 * 
 * @author: iohw
 * @date: 2025/4/16 22:24
 * @description:
 */
@Configuration
@Slf4j
public class PgVectorEmbeddingStoreInit {

    @Bean
    EmbeddingStore<TextSegment> initEmbeddingStore() {
        log.info("初始化InMemoryEmbeddingStore作为临时解决方案");
        return new InMemoryEmbeddingStore<>();
    }
}
