package com.chaitin.pandawiki.service.ai.assistant;

import com.chaitin.pandawiki.config.WebSearchProperties;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.RAGAssistant;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.SummarizeAssistant;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.WebSearchAssistant;
import com.chaitin.pandawiki.service.ai.config.ContentRetrieverFactory;
import com.chaitin.pandawiki.service.ai.config.PersistentChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.DefaultContentAggregator;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * AI助手服务
 * 提供RAG、Web搜索、总结等AI功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    final StreamingChatLanguageModel streamingChatLanguageModel;
    final ChatLanguageModel chatLanguageModel;
    final ContentRetrieverFactory contentRetrieverFactory;
    final PersistentChatMemoryStore chatMemoryStore;
    final WebSearchProperties webSearchProperties;

    private final Map<String, RAGAssistant> assistantCache = new ConcurrentHashMap<>();

    public RAGAssistant getRagAssistant(String memoryId, String kbId) {
        String key = memoryId + "_" + kbId;
        return assistantCache.computeIfAbsent(key, k -> {
            log.info("为会话 {} 创建RAG助手", memoryId);
            EmbeddingStoreContentRetriever contentRetriever = contentRetrieverFactory.createRetriever(memoryId, kbId);
            RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                    .contentRetriever(contentRetriever)
                    .contentAggregator(new DefaultContentAggregator())
                    .contentInjector(DefaultContentInjector.builder()
                            .promptTemplate(PromptTemplate.from("{{userMessage}}\n文档/文件/附件的内容如下，你可以基于下面的内容回答:\n{{contents}}"))
                            .build())
                    .build();

            return AiServices.builder(RAGAssistant.class)
                    .streamingChatLanguageModel(streamingChatLanguageModel)
                    .retrievalAugmentor(retrievalAugmentor)
                    .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                            .id(id)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .build();
        });
    }

    @Bean
    public SummarizeAssistant summarizeAssistant() {
        return AiServices.create(SummarizeAssistant.class, chatLanguageModel);
    }

    /**
     * Web搜索助手 - 只有在配置了有效API密钥时才创建
     * 当没有有效API密钥时，不创建此Bean，避免启动错误
     */
    @Bean
    @ConditionalOnProperty(
        name = "search.api-key",
        havingValue = "mock-api-key",
        matchIfMissing = false
    )
    public WebSearchAssistant webSearchAssistant() {
        String apiKey = webSearchProperties.getApiKey();

        log.info("创建WebSearchAssistant，API密钥: {}", apiKey != null ? "已配置" : "未配置");

        try {
            WebSearchEngine searchEngine = SearchApiWebSearchEngine.builder()
                    .apiKey(apiKey)
                    .engine(webSearchProperties.getEngine())
                    .build();

            EmbeddingStoreContentRetriever embeddingStoreContentRetriever =
                contentRetrieverFactory.createRetriever(null, null);

            WebSearchContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                    .webSearchEngine(searchEngine)
                    .maxResults(3)
                    .build();

            QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);

            RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                    .queryRouter(queryRouter)
                    .contentAggregator(new DefaultContentAggregator())
                    .contentInjector(DefaultContentInjector.builder()
                            .promptTemplate(PromptTemplate.from("{{userMessage}}\n文档/文件/附件的内容如下，你可以基于下面的内容回答:\n{{contents}}"))
                            .build())
                    .build();

            return AiServices.builder(WebSearchAssistant.class)
                    .streamingChatLanguageModel(streamingChatLanguageModel)
                    .retrievalAugmentor(retrievalAugmentor)
                    .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                            .id(id)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("创建WebSearchAssistant失败: {}", e.getMessage());
            throw e;
        }
    }
}
