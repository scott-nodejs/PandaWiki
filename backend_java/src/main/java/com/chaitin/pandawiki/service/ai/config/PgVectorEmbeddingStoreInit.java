package com.chaitin.pandawiki.service.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PgVectorEmbeddingStoreInit {

    final PgVectorProperties pgVectorProperties;

    @Bean
    EmbeddingStore<TextSegment> initEmbeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgVectorProperties.getHost())
                .port(pgVectorProperties.getPort())
                .user(pgVectorProperties.getUser())
                .password(pgVectorProperties.getPassword())
                .database(pgVectorProperties.getDatabase())
                .table(pgVectorProperties.getTable())
                .dimension(1024)
                .dropTableFirst(false)
                .createTable(true)
                .build();

    }
}
