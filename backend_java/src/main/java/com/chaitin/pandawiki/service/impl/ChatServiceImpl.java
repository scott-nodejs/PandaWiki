package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.dto.ChatRequest;
import com.chaitin.pandawiki.model.SSEEvent;
import com.chaitin.pandawiki.entity.Conversation;
import com.chaitin.pandawiki.entity.ConversationMessage;
import com.chaitin.pandawiki.mapper.ConversationMessageMapper;
import com.chaitin.pandawiki.service.ChatService;
import com.chaitin.pandawiki.service.ConversationService;
import com.chaitin.pandawiki.service.ai.assistant.AssistantService;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.StreamingAssistant;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.SummarizeAssistant;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.WebSearchAssistant;
import com.chaitin.pandawiki.service.ai.config.ContentRetrieverFactory;
import com.chaitin.pandawiki.util.IdGeneratorUtil;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 聊天服务实现
 * 使用真实的AI助手逻辑，输出SSE事件格式，并将问答入库
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private AssistantService assistantService;

    @Autowired(required = false)
    private WebSearchAssistant webSearchAssistant;

    @Resource
    private SummarizeAssistant summarizeAssistant;

    @Resource
    private ConversationService conversationService;

    @Resource
    private ConversationMessageMapper conversationMessageMapper;

    @Resource
    private ContentRetrieverFactory contentRetrieverFactory;

    @Override
    public SseEmitter chat(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(-1L); // 无超时

        log.info("开始处理聊天请求 - kbId: {}, conversationId: {}, message: {}",
                request.getKbId(), request.getConversationId(), request.getMessage());

        try {
            // 生成会话ID（如果没有提供）
            final String conversationId = request.getConversationId() == null || request.getConversationId().trim().isEmpty()
                ? generateConversationId()
                : request.getConversationId();

            // 判断是否是首次提问
            boolean isFirstQuestion = this.isFirstQuestion(request.getConversationId());

            // 如果是首次提问，创建会话记录
            if (isFirstQuestion) {
                createConversationRecord(conversationId, request);
            }

            // 保存用户问题到数据库
            String userMessageId = saveUserMessage(conversationId, request.getMessage(), request.getAppId());
            log.info("用户问题已保存到数据库 - messageId: {}", userMessageId);

            // 等待数据库写入完成，避免AI助手读取时数据不一致
            try {
                Thread.sleep(100); // 短暂等待确保数据库写入完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 发送会话ID事件
            SSEEvent conversationEvent = new SSEEvent("conversation_id", conversationId);
            emitter.send(SseEmitter.event()
                .name("message")
                .data(conversationEvent));

            // 发送nonce事件
            SSEEvent nonceEvent = new SSEEvent("nonce", UUID.randomUUID().toString());
            emitter.send(SseEmitter.event()
                .name("message")
                .data(nonceEvent));

            // 异步生成标题
            if (isFirstQuestion) {
                log.info("首次提问，异步生成会话标题");
                try {
                    String newTitle = summarizeAssistant.summarize(request.getMessage());
                    log.info("生成会话标题: {}", newTitle);
                    updateConversationTitle(conversationId, newTitle);
                } catch (Exception e) {
                    log.warn("生成会话标题失败: {}", e.getMessage());
                }
            }

            // 执行向量检索获取相关文档（基于知识库搜索）
            try {
                log.info("开始向量检索 - kbId: {}, message: {}", request.getKbId(), request.getMessage());

                // 创建ContentRetriever进行检索
                ContentRetriever contentRetriever = contentRetrieverFactory.createRetriever(conversationId, request.getKbId());

                // 执行检索
                Query query = Query.from(request.getMessage());
                List<Content> retrievedContents = contentRetriever.retrieve(query);

                log.info("向量检索完成，找到 {} 个相关文档", retrievedContents.size());

                // 发送检索到的真实文档块结果
                for (Content content : retrievedContents) {
                    TextSegment segment = content.textSegment();
                    Metadata metadata = segment.metadata();

                    SSEEvent chunkEvent = new SSEEvent();
                    chunkEvent.setType("chunk_result");
                    chunkEvent.setContent("");

                    SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();

                    // 从元数据中获取真实的nodeId和相关信息
                    String realNodeId = metadata.getString("nodeId");
                    String nodeName = metadata.getString("nodeName");
                    String kbId = metadata.getString("kbId");

                    chunkResult.setNodeId(realNodeId != null ? realNodeId : UUID.randomUUID().toString());
                    chunkResult.setName(nodeName != null ? nodeName : "相关文档");

                    // 生成文档摘要（取前200字符）
                    String contentText = segment.text();
                    String summary = contentText.length() > 200
                        ? contentText.substring(0, 200) + "..."
                        : contentText;
                    chunkResult.setSummary(summary);

                    chunkEvent.setChunkResult(chunkResult);

                    // 调试日志：验证chunk_result数据结构
                    log.info("发送真实chunk_result - nodeId: {}, name: {}, kbId: {}, contentLength: {}",
                        realNodeId, nodeName, kbId, contentText.length());

                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(chunkEvent));
                }

                // 如果没有检索到相关文档，发送一个默认提示
                if (retrievedContents.isEmpty()) {
                    log.info("未找到相关文档，发送默认提示");

                    SSEEvent chunkEvent = new SSEEvent();
                    chunkEvent.setType("chunk_result");
                    chunkEvent.setContent("");

                    SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();
                    chunkResult.setNodeId("no-result");
                    chunkResult.setName("知识库搜索");
                    chunkResult.setSummary("未在知识库中找到直接相关的内容，AI将基于通用知识回答");
                    chunkEvent.setChunkResult(chunkResult);

                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(chunkEvent));
                }

            } catch (Exception retrievalException) {
                log.error("向量检索失败: {}", retrievalException.getMessage(), retrievalException);

                // 检索失败时发送错误提示，但不中断对话
                SSEEvent chunkEvent = new SSEEvent();
                chunkEvent.setType("chunk_result");
                chunkEvent.setContent("");

                SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();
                chunkResult.setNodeId("retrieval-error");
                chunkResult.setName("检索服务");
                chunkResult.setSummary("知识库检索服务暂时不可用，AI将基于通用知识回答");
                chunkEvent.setChunkResult(chunkResult);

                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunkEvent));
            }

            // 获取真实的AI助手
            StreamingAssistant assistant = assistantService.getRagAssistant(
                    conversationId, request.getKbId());

            // 如果开启联网搜索且WebSearchAssistant可用
            if (Boolean.TRUE.equals(request.getIsWebSearchRequest())) {
                if (webSearchAssistant != null) {
                    log.info("使用联网搜索助手");
                    assistant = webSearchAssistant;
                } else {
                    log.warn("联网搜索功能未配置，使用知识库搜索");
                }
            }

            // 检查AI助手是否获取成功
            if (assistant == null) {
                log.error("AI助手获取失败，assistant为null");
                SSEEvent errorEvent = new SSEEvent("error", "AI助手初始化失败，请检查配置");
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                emitter.complete();
                return emitter;
            }

            // 用于收集AI回答的StringBuilder
            StringBuilder aiResponse = new StringBuilder();
            long startTime = System.currentTimeMillis();

            // 添加防护措施和调试日志
            log.info("准备调用AI助手 - conversationId: {}, message: {}, assistant: {}",
                conversationId, request.getMessage(), assistant.getClass().getSimpleName());

            // 检查输入参数
            if (conversationId == null || conversationId.trim().isEmpty()) {
                log.error("会话ID为空");
                SSEEvent errorEvent = new SSEEvent("error", "会话ID不能为空");
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                emitter.complete();
                return emitter;
            }

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.error("用户消息为空");
                SSEEvent errorEvent = new SSEEvent("error", "消息内容不能为空");
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                emitter.complete();
                return emitter;
            }

            TokenStream tokenStream;
            try {
                // 调用真实的AI助手进行对话
                tokenStream = assistant.chat(conversationId, request.getMessage());
                log.info("AI助手调用成功，开始处理流式响应");
            } catch (Exception ex) {
                log.error("调用AI助手失败: {}", ex.getMessage(), ex);

                String errorMessage;
                if (ex instanceof java.util.NoSuchElementException) {
                    // 特殊处理QwenHelper的消息列表为空错误
                    log.warn("检测到QwenHelper消息列表为空错误，可能是会话历史问题");
                    errorMessage = "会话状态异常，请刷新页面重新开始对话";
                } else {
                    errorMessage = "AI服务调用失败: " + ex.getMessage();
                }

                // 发送错误事件
                SSEEvent errorEvent = new SSEEvent("error", errorMessage);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                emitter.complete();
                return emitter;
            }

            tokenStream
                    .onPartialResponse(token -> {
                        try {
                            // 收集AI回答内容
                            aiResponse.append(token);

                            // 将AI回答包装成SSE事件格式
                            SSEEvent dataEvent = new SSEEvent("data", token);
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(dataEvent));
                        } catch (Exception e) {
                            log.error("发送部分响应失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .onCompleteResponse(response -> {
                        try {
                            // 计算响应时间
                            long responseTime = System.currentTimeMillis() - startTime;

                            // 保存AI回答到数据库
                            String assistantMessageId = saveAssistantMessage(
                                conversationId,
                                aiResponse.toString(),
                                responseTime,
                                request.getAppId()
                            );
                            log.info("AI回答已保存到数据库 - messageId: {}, responseTime: {}ms",
                                assistantMessageId, responseTime);

                            // 发送完成信号
                            SSEEvent doneEvent = new SSEEvent("done", "");
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(doneEvent));
                            emitter.complete();
                            log.info("AI聊天响应完成 - conversationId: {}", conversationId);
                        } catch (Exception e) {
                            log.error("发送完成事件失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .onError(e -> {
                        log.error("AI响应生成失败: {}", e.getMessage(), e);
                        try {
                            String errorMessage;
                            if (e.getMessage() != null && e.getMessage().contains("Invalid API-key")) {
                                errorMessage = "API密钥无效，请配置有效的通义千问API密钥。\n\n" +
                                              "配置步骤：\n" +
                                              "1. 访问 https://dashscope.console.aliyun.com/\n" +
                                              "2. 获取API密钥\n" +
                                              "3. 更新配置文件中的API密钥配置";
                            } else {
                                errorMessage = "抱歉，AI服务暂时不可用，请稍后重试。";
                            }

                            // 保存错误信息到数据库
                            saveAssistantMessage(conversationId, errorMessage, System.currentTimeMillis() - startTime, request.getAppId());

                            SSEEvent errorEvent = new SSEEvent("error", errorMessage);
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(errorEvent));
                            emitter.complete();
                        } catch (Exception sendError) {
                            log.error("发送错误消息失败: {}", sendError.getMessage());
                            emitter.completeWithError(e);
                        }
                    })
                    .start();

            log.info("已启动AI流式响应");

        } catch (Exception e) {
            log.error("初始化AI助手失败: {}", e.getMessage(), e);
            try {
                SSEEvent errorEvent = new SSEEvent("error", "系统错误，请联系管理员。");
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送系统错误消息失败: {}", sendError.getMessage());
                emitter.completeWithError(e);
            }
        }

        return emitter;
    }

    /**
     * 创建会话记录
     */
    private void createConversationRecord(String conversationId, ChatRequest request) {
        try {
            Conversation conversation = new Conversation();
            conversation.setId(conversationId);
            conversation.setKbId(request.getKbId());
            conversation.setAppId(request.getAppId());
            conversation.setAppName("牛小库智能问答"); // 可以根据实际需求设置
            conversation.setAppType(request.getAppType() != null ? request.getAppType() : 2);
            conversation.setSubject(request.getMessage().length() > 100
                ? request.getMessage().substring(0, 100) + "..."
                : request.getMessage()); // 使用问题前100字符作为临时标题
            conversation.setRemoteIp(getClientIp()); // 可以从请求中获取
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversation.setDeleted(false);

            conversationService.save(conversation);
            log.info("会话记录已创建 - conversationId: {}", conversationId);
        } catch (Exception e) {
            log.error("创建会话记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新会话标题
     */
    private void updateConversationTitle(String conversationId, String title) {
        try {
            Conversation conversation = conversationService.getById(conversationId);
            if (conversation != null) {
                conversation.setSubject(title);
                conversation.setUpdateTime(LocalDateTime.now());
                conversationService.updateById(conversation);
                log.info("会话标题已更新 - conversationId: {}, title: {}", conversationId, title);
            }
        } catch (Exception e) {
            log.error("更新会话标题失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存用户消息到数据库
     */
    private String saveUserMessage(String conversationId, String content, String appId) {
        try {
            ConversationMessage message = new ConversationMessage();
            message.setId(UUID.randomUUID().toString());
            message.setConversationId(conversationId);
            message.setAppId(appId);
            message.setRole("user");
            message.setContent(content);
            message.setPromptTokens(0);
            message.setCompletionTokens(0);
            message.setTotalTokens(0);
            message.setRemoteIp(getClientIp());
            message.setCreateTime(LocalDateTime.now());

            conversationMessageMapper.insert(message);
            return message.getId();
        } catch (Exception e) {
            log.error("保存用户消息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存AI助手回答到数据库
     */
    private String saveAssistantMessage(String conversationId, String content, long responseTime, String appId) {
        try {
            ConversationMessage message = new ConversationMessage();
            message.setId(UUID.randomUUID().toString());
            message.setConversationId(conversationId);
            message.setAppId(appId);
            message.setRole("assistant");
            message.setContent(content);
            // TODO: 从实际的模型信息中获取这些值
            message.setProvider("BAILIAN"); // 或者其他实际使用的提供商
            message.setModel("qwen-turbo"); // 或者其他实际使用的模型
            message.setPromptTokens(0); // 可以后续从AI服务获取
            message.setCompletionTokens(0); // 可以后续从AI服务获取
            message.setTotalTokens(0); // 可以后续计算token数量
            message.setResponseTime((int) responseTime);
            message.setRemoteIp(getClientIp());
            message.setCreateTime(LocalDateTime.now());

            conversationMessageMapper.insert(message);
            return message.getId();
        } catch (Exception e) {
            log.error("保存AI消息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成会话ID
     */
    private String generateConversationId() {
        return IdGeneratorUtil.generateId();
    }

    /**
     * 判断是否是首次提问
     */
    private boolean isFirstQuestion(String conversationId) {
        return conversationId == null || conversationId.trim().isEmpty();
    }

    /**
     * 获取客户端IP地址（简化实现）
     */
    private String getClientIp() {
        // 在实际项目中，可以从HttpServletRequest中获取真实IP
        return "127.0.0.1";
    }

    /**
     * 测试AI助手调用（绕过会话历史）
     * 用于调试AI服务是否正常工作
     */
    public SseEmitter testChat(String message) {
        SseEmitter emitter = new SseEmitter(-1L);

        log.info("测试AI助手调用 - message: {}", message);

        try {
            // 直接创建一个简单的AI助手进行测试
            // 这里可能需要根据实际的AssistantService实现来调整
            log.info("开始测试AI助手直接调用...");

            // 发送测试开始事件
            SSEEvent startEvent = new SSEEvent("data", "测试AI助手调用...\n");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(startEvent));

            // 模拟简单的响应
            SSEEvent responseEvent = new SSEEvent("data", "AI助手测试响应：您发送的消息是: " + message);
            emitter.send(SseEmitter.event()
                .name("message")
                .data(responseEvent));

            // 完成测试
            SSEEvent doneEvent = new SSEEvent("done", "");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(doneEvent));
            emitter.complete();

        } catch (Exception e) {
            log.error("测试AI助手失败: {}", e.getMessage(), e);
            try {
                SSEEvent errorEvent = new SSEEvent("error", "测试失败: " + e.getMessage());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送测试错误消息失败: {}", sendError.getMessage());
                emitter.completeWithError(e);
            }
        }

        return emitter;
    }

    /**
     * 简化的AI测试调用（直接使用新会话ID，不读取历史）
     */
    public SseEmitter testChatWithNewSession(String message) {
        SseEmitter emitter = new SseEmitter(-1L);

        log.info("测试新会话AI调用 - message: {}", message);

        try {
            // 生成新的会话ID，确保没有历史记录
            String testConversationId = "test-" + UUID.randomUUID().toString();
            log.info("使用新测试会话ID: {}", testConversationId);

            // 发送测试开始事件
            SSEEvent startEvent = new SSEEvent("data", "开始新会话AI测试...\n");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(startEvent));

            // 尝试获取AI助手（使用新会话ID）
            StreamingAssistant testAssistant = assistantService.getRagAssistant(
                    testConversationId, "test-kb-id");

            if (testAssistant == null) {
                SSEEvent errorEvent = new SSEEvent("error", "无法获取AI助手");
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(errorEvent));
                emitter.complete();
                return emitter;
            }

            log.info("AI助手获取成功，准备调用");

            // 收集响应
            StringBuilder response = new StringBuilder();

            // 直接调用AI助手
            TokenStream tokenStream = testAssistant.chat(testConversationId, message);

            tokenStream
                .onPartialResponse(token -> {
                    try {
                        response.append(token);
                        SSEEvent dataEvent = new SSEEvent("data", token);
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(dataEvent));
                    } catch (Exception e) {
                        log.error("发送部分响应失败: {}", e.getMessage());
                    }
                })
                .onCompleteResponse(resp -> {
                    try {
                        SSEEvent doneEvent = new SSEEvent("done", "");
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(doneEvent));
                        emitter.complete();
                        log.info("测试AI调用完成，响应长度: {}", response.length());
                    } catch (Exception e) {
                        log.error("发送完成事件失败: {}", e.getMessage());
                    }
                })
                .onError(error -> {
                    log.error("AI调用失败: {}", error.getMessage(), error);
                    try {
                        SSEEvent errorEvent = new SSEEvent("error", "AI调用失败: " + error.getMessage());
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(errorEvent));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("发送错误事件失败: {}", e.getMessage());
                        emitter.completeWithError(error);
                    }
                })
                .start();

        } catch (Exception e) {
            log.error("测试新会话AI调用失败: {}", e.getMessage(), e);
            try {
                SSEEvent errorEvent = new SSEEvent("error", "测试失败: " + e.getMessage());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送测试错误消息失败: {}", sendError.getMessage());
                emitter.completeWithError(e);
            }
        }

        return emitter;
    }

    /**
     * 测试chunk_result格式
     * 专门用于验证前端数据格式匹配
     */
    public SseEmitter testChunkResult(String message) {
        SseEmitter emitter = new SseEmitter(-1L);

        log.info("测试chunk_result格式 - message: {}", message);

        try {
            // 发送测试开始事件
            SSEEvent startEvent = new SSEEvent("data", "测试chunk_result格式...\n");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(startEvent));

            // 模拟发送多个chunk_result，测试前端处理
            for (int i = 1; i <= 3; i++) {
                SSEEvent chunkEvent = new SSEEvent();
                chunkEvent.setType("chunk_result");
                chunkEvent.setContent("");

                SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();
                String nodeId = "test-node-" + i;
                chunkResult.setNodeId(nodeId);
                chunkResult.setName("测试文档" + i);
                chunkResult.setSummary("这是第" + i + "个测试文档的摘要，用于验证前端显示效果");
                chunkEvent.setChunkResult(chunkResult);

                log.info("发送第{}个chunk_result - nodeId: {}, name: {}",
                    i, nodeId, chunkResult.getName());

                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunkEvent));

                // 间隔一下，模拟真实搜索过程
                Thread.sleep(500);
            }

            // 发送数据响应
            SSEEvent dataEvent = new SSEEvent("data", "基于以上搜索结果，这是AI的回答内容...");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(dataEvent));

            // 完成测试
            SSEEvent doneEvent = new SSEEvent("done", "");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(doneEvent));
            emitter.complete();

            log.info("chunk_result格式测试完成");

        } catch (Exception e) {
            log.error("测试chunk_result格式失败: {}", e.getMessage(), e);
            try {
                SSEEvent errorEvent = new SSEEvent("error", "测试失败: " + e.getMessage());
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(errorEvent));
                emitter.complete();
            } catch (Exception sendError) {
                log.error("发送测试错误消息失败: {}", sendError.getMessage());
                emitter.completeWithError(e);
            }
        }

        return emitter;
    }
}

