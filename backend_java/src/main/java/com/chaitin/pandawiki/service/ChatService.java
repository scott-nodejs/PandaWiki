package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.dto.ChatRequest;
import com.chaitin.pandawiki.model.SSEEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务接口
 * 
 * @author chaitin
 */
public interface ChatService {
    
    /**
     * 处理聊天请求
     * 
     * @param request 聊天请求
     * @return SSE发射器
     */
    SseEmitter chat(ChatRequest request);
} 