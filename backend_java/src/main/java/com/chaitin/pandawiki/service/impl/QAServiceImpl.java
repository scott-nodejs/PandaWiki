package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chaitin.pandawiki.dto.QAListRequest;
import com.chaitin.pandawiki.dto.QAListResponse;
import com.chaitin.pandawiki.entity.Conversation;
import com.chaitin.pandawiki.entity.ConversationMessage;
import com.chaitin.pandawiki.mapper.ConversationMapper;
import com.chaitin.pandawiki.mapper.ConversationMessageMapper;
import com.chaitin.pandawiki.service.QAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QA服务实现类
 * 
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QAServiceImpl implements QAService {
    
    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper conversationMessageMapper;
    
    @Override
    public QAListResponse getQAList(QAListRequest request) {
        log.info("获取QA列表 - kbId: {}, keyword: {}, page: {}, perPage: {}", 
            request.getKbId(), request.getKeyword(), request.getPage(), request.getPerPage());
        
        // 构建会话查询条件
        LambdaQueryWrapper<Conversation> conversationWrapper = new LambdaQueryWrapper<>();
        conversationWrapper.eq(Conversation::getKbId, request.getKbId());
        
        if (StringUtils.hasText(request.getKeyword())) {
            conversationWrapper.like(Conversation::getSubject, request.getKeyword());
        }
        
        // 时间范围查询
        if (StringUtils.hasText(request.getStartTime())) {
            LocalDateTime startTime = parseDateTime(request.getStartTime());
            if (startTime != null) {
                conversationWrapper.ge(Conversation::getCreateTime, startTime);
            }
        }
        
        if (StringUtils.hasText(request.getEndTime())) {
            LocalDateTime endTime = parseDateTime(request.getEndTime());
            if (endTime != null) {
                conversationWrapper.le(Conversation::getCreateTime, endTime);
            }
        }
        
        conversationWrapper.orderByDesc(Conversation::getCreateTime);
        
        // 查询会话列表
        List<Conversation> conversations = conversationMapper.selectList(conversationWrapper);
        
        // 分页处理
        int total = conversations.size();
        int start = (request.getPage() - 1) * request.getPerPage();
        int end = Math.min(start + request.getPerPage(), total);
        
        List<Conversation> pagedConversations = conversations.subList(start, end);
        
        // 构建QA响应列表
        List<QAListResponse.QAItem> qaItems = new ArrayList<>();
        
        for (Conversation conversation : pagedConversations) {
            List<QAListResponse.QAItem> items = buildQAItemsFromConversation(conversation, request.getKeyword());
            qaItems.addAll(items);
        }
        
        QAListResponse response = new QAListResponse();
        response.setData(qaItems);
        response.setTotal((long) total);
        
        log.info("获取QA列表成功 - total: {}", total);
        return response;
    }
    
    @Override
    public QAListResponse getQAByConversationId(String conversationId) {
        log.info("根据会话ID获取QA记录 - conversationId: {}", conversationId);
        
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return new QAListResponse();
        }
        
        List<QAListResponse.QAItem> qaItems = buildQAItemsFromConversation(conversation, null);
        
        QAListResponse response = new QAListResponse();
        response.setData(qaItems);
        response.setTotal((long) qaItems.size());
        
        return response;
    }
    
    @Override
    public void deleteQAByConversationId(String conversationId) {
        log.info("删除QA记录 - conversationId: {}", conversationId);
        
        // 删除会话消息
        LambdaQueryWrapper<ConversationMessage> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(ConversationMessage::getConversationId, conversationId);
        conversationMessageMapper.delete(messageWrapper);
        
        // 删除会话
        conversationMapper.deleteById(conversationId);
        
        log.info("QA记录删除成功 - conversationId: {}", conversationId);
    }
    
    @Override
    public QAListResponse searchQA(String kbId, String keyword, Integer pageNum, Integer pageSize) {
        log.info("搜索QA记录 - kbId: {}, keyword: {}, pageNum: {}, pageSize: {}", 
            kbId, keyword, pageNum, pageSize);
        
        QAListRequest request = new QAListRequest();
        request.setKbId(kbId);
        request.setKeyword(keyword);
        request.setPage(pageNum != null ? pageNum : 1);
        request.setPerPage(pageSize != null ? pageSize : 10);
        
        return getQAList(request);
    }
    
    /**
     * 从会话构建QA项列表
     */
    private List<QAListResponse.QAItem> buildQAItemsFromConversation(Conversation conversation, String keyword) {
        List<QAListResponse.QAItem> qaItems = new ArrayList<>();
        
        // 查询该会话的所有消息
        LambdaQueryWrapper<ConversationMessage> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(ConversationMessage::getConversationId, conversation.getId())
                     .orderByAsc(ConversationMessage::getCreateTime);
        
        List<ConversationMessage> messages = conversationMessageMapper.selectList(messageWrapper);
        
        // 将消息按照用户问题和AI回答配对
        Map<String, ConversationMessage> userMessages = new HashMap<>();
        Map<String, ConversationMessage> assistantMessages = new HashMap<>();
        
        for (ConversationMessage message : messages) {
            if ("user".equals(message.getRole())) {
                userMessages.put(message.getId(), message);
            } else if ("assistant".equals(message.getRole())) {
                assistantMessages.put(message.getId(), message);
            }
        }
        
        // 简单配对：按时间顺序配对用户问题和AI回答
        List<ConversationMessage> userMsgList = messages.stream()
                .filter(m -> "user".equals(m.getRole()))
                .collect(Collectors.toList());
        
        List<ConversationMessage> assistantMsgList = messages.stream()
                .filter(m -> "assistant".equals(m.getRole()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < userMsgList.size(); i++) {
            ConversationMessage userMsg = userMsgList.get(i);
            ConversationMessage assistantMsg = i < assistantMsgList.size() ? assistantMsgList.get(i) : null;
            
            // 如果有关键词过滤，检查问题或答案是否包含关键词
            if (StringUtils.hasText(keyword)) {
                boolean matchQuestion = userMsg.getContent().contains(keyword);
                boolean matchAnswer = assistantMsg != null && assistantMsg.getContent().contains(keyword);
                if (!matchQuestion && !matchAnswer) {
                    continue;
                }
            }
            
            QAListResponse.QAItem qaItem = new QAListResponse.QAItem();
            qaItem.setConversationId(conversation.getId());
            qaItem.setSubject(conversation.getSubject());
            qaItem.setQuestion(userMsg.getContent());
            qaItem.setAnswer(assistantMsg != null ? assistantMsg.getContent() : "");
            qaItem.setResponseTime(assistantMsg != null ? assistantMsg.getResponseTime() : 0);
            qaItem.setTotalTokens(assistantMsg != null ? assistantMsg.getTotalTokens() : 0L);
            qaItem.setCreateTime(userMsg.getCreateTime());
            qaItem.setKbId(conversation.getKbId());
            
            qaItems.add(qaItem);
        }
        
        return qaItems;
    }
    
    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            log.warn("解析日期时间失败: {}", dateTimeStr);
            return null;
        }
    }
} 