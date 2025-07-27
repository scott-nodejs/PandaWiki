package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.dto.ChatRequest;
import com.chaitin.pandawiki.model.SSEEvent;
import com.chaitin.pandawiki.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 聊天服务实现
 * 使用完全独立的异步处理机制，不依赖Shiro SecurityManager
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final AsyncTaskExecutor sseExecutor;

    /**
     * 构造函数，注入SSE专用线程池
     */
    public ChatServiceImpl(@Qualifier("sseTaskExecutor") AsyncTaskExecutor sseExecutor) {
        this.sseExecutor = sseExecutor;
    }

    @Override
    public SseEmitter chat(ChatRequest request) {
        // 创建SSE连接，设置30分钟超时
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        
        log.info("开始处理聊天请求：{}", request);
        
        // 设置完成回调
        emitter.onCompletion(() -> {
            if (isCompleted.compareAndSet(false, true)) {
                log.info("SSE连接正常完成");
            }
        });
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            if (isCompleted.compareAndSet(false, true)) {
                log.warn("SSE连接超时");
                emitter.complete();
            }
        });
        
        // 设置错误回调
        emitter.onError((ex) -> {
            if (isCompleted.compareAndSet(false, true)) {
                log.error("SSE连接发生错误: {}", ex.getMessage());
                emitter.completeWithError(ex);
            }
        });
        
        // 使用独立线程池异步处理，避免Shiro相关问题
        CompletableFuture.runAsync(() -> {
            try {
                simulateAIResponse(emitter, request, isCompleted);
            } catch (Exception e) {
                if (isCompleted.compareAndSet(false, true)) {
                    log.error("处理聊天请求时发生异常", e);
                    try {
                        // 发送错误事件，使用正确的JSON格式
                        SSEEvent errorEvent = new SSEEvent("error", "处理请求时发生错误: " + e.getMessage());
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(errorEvent));
                        emitter.complete();
                    } catch (Exception sendError) {
                        log.error("发送错误消息失败", sendError);
                        emitter.completeWithError(sendError);
                    }
                }
            }
        }, sseExecutor);
        
        return emitter;
    }

    /**
     * 模拟AI响应过程
     */
    private void simulateAIResponse(SseEmitter emitter, ChatRequest request, AtomicBoolean isCompleted) throws Exception {
        String message = request.getMessage();
        log.info("开始处理聊天消息 - kbId: {}, message: {}", request.getKbId(), message);
        
        // 发送会话ID事件
        if (!isCompleted.get()) {
            SSEEvent conversationEvent = new SSEEvent("conversation_id", "01983c42-5d5d-71ca-b53a-a06b19920ebd");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(conversationEvent));
        }
        
        // 发送nonce事件
        if (!isCompleted.get()) {
            SSEEvent nonceEvent = new SSEEvent("nonce", "29e11650-ad49-4a43-9709-0a7bb8d0047e");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(nonceEvent));
        }
        
        // 模拟文档块结果
        if (!isCompleted.get()) {
            SSEEvent chunkEvent = new SSEEvent();
            chunkEvent.setType("chunk_result");
            chunkEvent.setContent("");
            
            SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();
            chunkResult.setNodeId("019735bd-8a73-775e-902f-10dd2a243d5e");
            chunkResult.setName("三国志 / 董二袁刘传");
            chunkResult.setSummary("《三国志·魏书·董二袁刘传》记载了董卓、袁绍、袁术、刘表四位汉末重要人物的生平事迹...");
            chunkEvent.setChunkResult(chunkResult);
            
            emitter.send(SseEmitter.event()
                .name("message")
                .data(chunkEvent));
        }
        
        // 模拟AI逐步响应
        String[] responseChunks = {
            "根据您的问题：" + message,
            "，我来为您详细解答。",
            "\n\n基于我的知识库搜索结果，",
            "我找到了相关的信息。",
            "\n\n希望这些信息对您有所帮助！",
            "如果您还有其他问题，请随时告诉我。"
        };
        
        for (int i = 0; i < responseChunks.length && !isCompleted.get(); i++) {
            Thread.sleep(800); // 模拟生成延迟
            
            if (!isCompleted.get()) {
                SSEEvent dataEvent = new SSEEvent("data", responseChunks[i]);
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(dataEvent));
            }
        }
        
        // 发送完成信号
        if (isCompleted.compareAndSet(false, true)) {
            SSEEvent doneEvent = new SSEEvent("done", "");
            emitter.send(SseEmitter.event()
                .name("message")
                .data(doneEvent));
            emitter.complete();
            log.info("AI响应完成，SSE连接已关闭");
        }
    }

    /**
     * 处理聊天消息的核心逻辑
     * 使用独立线程池处理，避免Shiro SecurityManager问题
     */
    public void processChatMessage(SseEmitter emitter, String message) {
        sseExecutor.submit(() -> {
            try {
                log.info("开始处理聊天消息: {}", message);
                
                AtomicBoolean isCompleted = new AtomicBoolean(false);
                
                // 发送会话ID事件
                if (!isCompleted.get()) {
                    SSEEvent conversationEvent = new SSEEvent("conversation_id", "01983c42-5d5d-71ca-b53a-a06b19920ebd");
                    log.info("发送conversation_id事件: {}", conversationEvent);
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(conversationEvent));
                }
                
                // 发送nonce事件
                if (!isCompleted.get()) {
                    SSEEvent nonceEvent = new SSEEvent("nonce", "29e11650-ad49-4a43-9709-0a7bb8d0047e");
                    log.info("发送nonce事件: {}", nonceEvent);
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(nonceEvent));
                }
                
                // 模拟文档块结果
                if (!isCompleted.get()) {
                    SSEEvent chunkEvent = new SSEEvent();
                    chunkEvent.setType("chunk_result");
                    chunkEvent.setContent("");
                    
                    SSEEvent.ChunkResult chunkResult = new SSEEvent.ChunkResult();
                    chunkResult.setNodeId("019735bd-8a73-775e-902f-10dd2a243d5e");
                    chunkResult.setName("三国志 / 董二袁刘传");
                    chunkResult.setSummary("《三国志·魏书·董二袁刘传》记载了董卓、袁绍、袁术、刘表四位汉末重要人物的生平事迹...");
                    chunkEvent.setChunkResult(chunkResult);
                    
                    log.info("发送chunk_result事件: {}", chunkEvent);
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(chunkEvent));
                }
                
                // 模拟AI逐步响应
                String[] responseChunks = {
                    "根据您的问题：" + message,
                    "，我来为您详细解答。",
                    "\n\n基于我的知识库搜索结果，",
                    "我找到了相关的信息。",
                    "\n\n希望这些信息对您有所帮助！",
                    "如果您还有其他问题，请随时告诉我。"
                };
                
                for (int i = 0; i < responseChunks.length && !isCompleted.get(); i++) {
                    Thread.sleep(800); // 模拟生成延迟
                    
                    if (!isCompleted.get()) {
                        SSEEvent dataEvent = new SSEEvent("data", responseChunks[i]);
                        log.info("发送data事件 [{}]: {}", i, dataEvent);
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(dataEvent));
                    }
                }
                
                // 发送完成信号
                if (isCompleted.compareAndSet(false, true)) {
                    SSEEvent doneEvent = new SSEEvent("done", "");
                    log.info("发送done事件: {}", doneEvent);
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(doneEvent));
                    emitter.complete();
                    log.info("AI响应完成，SSE连接已关闭");
                }
                
            } catch (Exception e) {
                log.error("处理聊天消息时发生异常", e);
                try {
                    SSEEvent errorEvent = new SSEEvent("error", "处理消息时发生错误: " + e.getMessage());
                    emitter.send(SseEmitter.event()
                        .name("message") 
                        .data(errorEvent));
                    emitter.completeWithError(e);
                } catch (Exception sendError) {
                    log.error("发送错误事件失败", sendError);
                    emitter.completeWithError(sendError);
                }
            }
        });
    }
} 