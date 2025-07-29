package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.dto.ConversationListRequest;
import com.chaitin.pandawiki.dto.ConversationListResponse;
import com.chaitin.pandawiki.entity.ConversationMessage;
import com.chaitin.pandawiki.service.ConversationService;
import com.chaitin.pandawiki.service.QAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 * 按照Go后端的API设计，提供会话列表和详情查询
 * 
 * @author chaitin
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationService conversationService;
    private final QAService qaService;
    
    /**
     * 获取会话列表
     * 与Go后端接口保持一致：GET /api/v1/conversation
     * 
     * @param kbId 知识库ID
     * @param appId 应用ID（可选）
     * @param subject 会话主题（可选）
     * @param remoteIp 远程IP（可选）
     * @param page 页码，默认1
     * @param perPage 每页大小，默认10
     * @return 会话列表响应
     */
    @GetMapping("")
    public ConversationListResponse getConversationList(
            @RequestParam("kb_id") String kbId,
            @RequestParam(value = "app_id", required = false) String appId,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "remote_ip", required = false) String remoteIp,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "per_page", defaultValue = "10") Integer perPage) {
        
        log.info("获取会话列表 - kbId: {}, appId: {}, page: {}, perPage: {}", 
            kbId, appId, page, perPage);
        
        ConversationListRequest request = new ConversationListRequest();
        request.setKbId(kbId);
        request.setAppId(appId);
        request.setSubject(subject);
        request.setRemoteIp(remoteIp);
        request.setPage(page);
        request.setPerPage(perPage);
        
        return conversationService.getConversationList(request);
    }
    
    /**
     * 获取会话详情（包含完整的QA记录）
     * 与Go后端接口保持一致：GET /api/v1/conversation/detail?id=xxx
     * 
     * @param id 会话ID
     * @return 会话详情响应
     */
    @GetMapping("/detail")
    public ConversationDetailResponse getConversationDetail(@RequestParam("id") String id) {
        log.info("获取会话详情 - conversationId: {}", id);
        
        // 获取会话基本信息
        var conversation = conversationService.getById(id);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }
        
        // 获取QA记录
        var qaResponse = qaService.getQAByConversationId(id);
        
        ConversationDetailResponse response = new ConversationDetailResponse();
        response.setId(conversation.getId());
        response.setAppId(conversation.getAppId());
        response.setSubject(conversation.getSubject());
        response.setRemoteIp(conversation.getRemoteIp());
        response.setCreatedAt(conversation.getCreateTime());
        response.setMessages(convertQAToMessages(qaResponse.getData()));
        
        return response;
    }
    
    /**
     * 将QA记录转换为消息列表
     */
    private List<ConversationMessage> convertQAToMessages(List<com.chaitin.pandawiki.dto.QAListResponse.QAItem> qaItems) {
        return qaItems.stream()
            .flatMap(qa -> {
                // 用户消息
                ConversationMessage userMsg = new ConversationMessage();
                userMsg.setConversationId(qa.getConversationId());
                userMsg.setRole("user");
                userMsg.setContent(qa.getQuestion());
                userMsg.setCreateTime(qa.getCreateTime());
                
                // AI回答消息
                ConversationMessage assistantMsg = new ConversationMessage();
                assistantMsg.setConversationId(qa.getConversationId());
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(qa.getAnswer());
                assistantMsg.setTotalTokens(qa.getTotalTokens().intValue());
                assistantMsg.setResponseTime(qa.getResponseTime());
                assistantMsg.setCreateTime(qa.getCreateTime());
                
                return java.util.stream.Stream.of(userMsg, assistantMsg);
            })
            .toList();
    }
    
    /**
     * 会话详情响应
     * 按照Go后端的ConversationDetailResp结构设计
     */
    @lombok.Data
    public static class ConversationDetailResponse {
        private String id;
        private String appId;
        private String subject;
        private String remoteIp;
        private List<ConversationMessage> messages;
        private java.time.LocalDateTime createdAt;
    }
}
