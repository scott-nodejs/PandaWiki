package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.model.SSEEvent;
import com.chaitin.pandawiki.service.NodeService;
import com.chaitin.pandawiki.dto.CreateNodeRequest;
import com.chaitin.pandawiki.service.impl.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 测试聊天控制器
 * 用于验证ChatService修复是否有效
 * 
 * @author chaitin
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestChatController {
    
    private final ChatServiceImpl chatService;
    private final NodeService nodeService;
    
    /**
     * 测试AI助手调用
     * 
     * @param message 测试消息
     * @return SSE响应
     */
    @GetMapping("/chat")
    public SseEmitter testChat(@RequestParam(defaultValue = "Hello, AI!") String message) {
        log.info("收到测试聊天请求: {}", message);
        return chatService.testChat(message);
    }
    
    /**
     * 测试新会话AI调用（绕过历史记录）
     * 
     * @param message 测试消息
     * @return SSE响应
     */
    @GetMapping("/chat-new")
    public SseEmitter testChatWithNewSession(@RequestParam(defaultValue = "Hello, New Session!") String message) {
        log.info("收到新会话测试聊天请求: {}", message);
        return chatService.testChatWithNewSession(message);
    }
    
    /**
     * 测试chunk_result格式的接口
     * 
     * @param message 测试消息
     * @return SSE响应
     */
    @GetMapping("/chunk-result")
    public SseEmitter testChunkResult(@RequestParam(defaultValue = "Hello, Chunk Result!") String message) {
        log.info("收到chunk_result测试请求: {}", message);
        return chatService.testChunkResult(message);
    }
    
    /**
     * 健康检查
     * 
     * @return 状态信息
     */
    @GetMapping("/health")
    public String health() {
        return "ChatService测试接口正常运行";
    }
    
    /**
     * 测试节点向量化存储
     * 
     * @param content 测试内容
     * @return 创建结果
     */
    @PostMapping("/node-vector")
    public String testNodeVector(@RequestParam(defaultValue = "这是一个测试文档，用于验证向量化存储功能。") String content) {
        log.info("收到节点向量化测试请求: {}", content);
        
        try {
            // 创建测试节点请求
            CreateNodeRequest request = new CreateNodeRequest();
            request.setName("测试向量化文档");
            request.setContent(content);
            request.setType(2); // 文档类型
            request.setKb_id("test-kb-001");
            request.setSummary("用于测试向量化存储功能的文档");
            
            // 创建节点（会自动触发向量化）
            String nodeId = nodeService.createNode(request);
            
            log.info("测试节点创建成功，nodeId: {}", nodeId);
            return "节点创建成功，已向量化存储。nodeId: " + nodeId;
            
        } catch (Exception e) {
            log.error("测试节点向量化失败: {}", e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }
    
    /**
     * 测试向量化搜索（通过知识库对话）
     * 
     * @param question 测试问题
     * @return SSE响应
     */
    @GetMapping("/vector-search")
    public SseEmitter testVectorSearch(@RequestParam(defaultValue = "请介绍一下向量化存储") String question) {
        log.info("收到向量搜索测试请求: {}", question);
        return chatService.testChatWithNewSession(question);
    }
} 