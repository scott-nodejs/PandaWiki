package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.entity.Conversation;
import com.chaitin.pandawiki.entity.ConversationMessage;
import com.chaitin.pandawiki.mapper.ConversationMapper;
import com.chaitin.pandawiki.mapper.ConversationMessageMapper;
import com.chaitin.pandawiki.service.ConversationService;
import com.chaitin.pandawiki.service.ai.config.PersistentChatMemoryStore;
import com.chaitin.pandawiki.dto.ConversationListRequest;
import com.chaitin.pandawiki.dto.ConversationListResponse;
import com.chaitin.pandawiki.dto.IPAddressInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 * 增强版：支持会话重置和异常消息清理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Autowired
    private ConversationMessageMapper conversationMessageMapper;
    
    @Autowired
    private PersistentChatMemoryStore chatMemoryStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createConversation(Conversation conversation) {
        conversation.setId(UUID.randomUUID().toString());
        save(conversation);
        return conversation.getId();
    }

    @Override
    public Page<Conversation> pageConversations(String kbId, String appId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getKbId, kbId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
    
    @Override
    public ConversationListResponse getConversationList(ConversationListRequest request) {
        log.info("获取对话列表 - kbId: {}, page: {}, perPage: {}", 
            request.getKbId(), request.getPage(), request.getPerPage());
        
        // 构建查询条件
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getKbId, request.getKbId());
        
        // 可选条件
        if (StringUtils.hasText(request.getAppId())) {
            queryWrapper.eq(Conversation::getAppId, request.getAppId());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.like(Conversation::getSubject, request.getSubject());
        }
        if (StringUtils.hasText(request.getRemoteIp())) {
            queryWrapper.like(Conversation::getRemoteIp, request.getRemoteIp());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Conversation::getCreateTime);
        
        // 分页查询
        Page<Conversation> page = new Page<>(request.getPage(), request.getPerPage());
        Page<Conversation> conversationPage = page(page, queryWrapper);
        
        // 转换为响应对象
        List<ConversationListResponse.ConversationItem> conversationItems = conversationPage.getRecords()
            .stream()
            .map(this::convertToConversationItem)
            .collect(Collectors.toList());
        
        ConversationListResponse response = new ConversationListResponse();
        response.setData(conversationItems);
        response.setTotal(conversationPage.getTotal());
        
        log.info("获取对话列表成功: total={}", conversationPage.getTotal());
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetConversationState(String conversationId) {
        log.info("开始重置会话状态: {}", conversationId);
        
        try {
            // 1. 清理聊天记忆存储的缓存
            chatMemoryStore.deleteMessages(conversationId);
            log.info("已清理会话 {} 的聊天记忆缓存", conversationId);
            
            // 2. 删除数据库中的会话消息
            LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ConversationMessage::getConversationId, conversationId);
            int deletedCount = conversationMessageMapper.delete(queryWrapper);
            log.info("已删除会话 {} 的数据库消息数量: {}", conversationId, deletedCount);
            
            // 3. 更新会话的最后更新时间
            Conversation conversation = getById(conversationId);
            if (conversation != null) {
                conversation.setUpdateTime(LocalDateTime.now());
                updateById(conversation);
                log.info("已更新会话 {} 的时间戳", conversationId);
            }
            
            log.info("会话状态重置成功: {}", conversationId);
            return true;
            
        } catch (Exception e) {
            log.error("重置会话状态失败: {}", conversationId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupCorruptedMessages(String conversationId) {
        log.info("开始清理会话 {} 的异常消息", conversationId);
        
        try {
            // 查询所有消息
            LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ConversationMessage::getConversationId, conversationId)
                       .orderByAsc(ConversationMessage::getCreateTime);
            
            List<ConversationMessage> messages = conversationMessageMapper.selectList(queryWrapper);
            log.info("会话 {} 当前消息总数: {}", conversationId, messages.size());
            
            int cleanedCount = 0;
            
            // 清理策略
            List<String> toDeleteIds = new ArrayList<>();
            String lastUserMessage = null;
            String lastAiMessage = null;
            
            for (ConversationMessage message : messages) {
                boolean shouldDelete = false;
                
                // 1. 清理空内容消息
                if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                    shouldDelete = true;
                    log.debug("标记删除空内容消息: {}", message.getId());
                }
                
                // 2. 清理重复的用户消息
                else if ("user".equals(message.getRole())) {
                    if (message.getContent().equals(lastUserMessage)) {
                        shouldDelete = true;
                        log.debug("标记删除重复用户消息: {}", message.getContent());
                    } else {
                        lastUserMessage = message.getContent();
                    }
                }
                
                // 3. 清理重复的AI消息
                else if ("assistant".equals(message.getRole())) {
                    if (message.getContent().equals(lastAiMessage)) {
                        shouldDelete = true;
                        log.debug("标记删除重复AI消息");
                    } else {
                        lastAiMessage = message.getContent();
                    }
                }
                
                // 4. 清理格式异常的工具消息
                else if ("tool".equals(message.getRole())) {
                    if (!isValidToolMessage(message.getContent())) {
                        shouldDelete = true;
                        log.debug("标记删除异常工具消息: {}", message.getId());
                    }
                }
                
                if (shouldDelete) {
                    toDeleteIds.add(message.getId());
                    cleanedCount++;
                }
            }
            
            // 批量删除异常消息
            if (!toDeleteIds.isEmpty()) {
                LambdaQueryWrapper<ConversationMessage> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.in(ConversationMessage::getId, toDeleteIds);
                conversationMessageMapper.delete(deleteWrapper);
                log.info("已删除 {} 条异常消息", toDeleteIds.size());
                
                // 清理聊天记忆缓存，强制重新加载
                chatMemoryStore.deleteMessages(conversationId);
                log.info("已清理会话 {} 的聊天记忆缓存", conversationId);
            }
            
            log.info("会话 {} 异常消息清理完成，清理数量: {}", conversationId, cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理会话 {} 异常消息失败", conversationId, e);
            return 0;
        }
    }
    
    /**
     * 验证工具消息格式是否正确
     */
    private boolean isValidToolMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // 简单检查工具消息格式
        return content.contains("id:") && content.contains("tool_name:") && content.contains("execution_result:");
    }
    
    /**
     * 转换为对话列表项
     */
    private ConversationListResponse.ConversationItem convertToConversationItem(Conversation conversation) {
        ConversationListResponse.ConversationItem item = new ConversationListResponse.ConversationItem();
        item.setId(conversation.getId());
        item.setAppName(conversation.getAppName() != null ? conversation.getAppName() : "");
        item.setAppType(conversation.getAppType() != null ? conversation.getAppType() : 2);
        item.setSubject(conversation.getSubject());
        item.setRemoteIp(conversation.getRemoteIp());
        item.setCreatedAt(conversation.getCreateTime());
        
        // 转换用户信息
        if (conversation.getInfo() != null) {
            ConversationListResponse.ConversationInfo info = new ConversationListResponse.ConversationInfo();
            if (conversation.getInfo().getUserInfo() != null) {
                ConversationListResponse.UserInfo userInfo = new ConversationListResponse.UserInfo();
                Conversation.UserInfo originalUserInfo = conversation.getInfo().getUserInfo();
                
                userInfo.setUserId(originalUserInfo.getUserId() != null ? originalUserInfo.getUserId() : "");
                userInfo.setName(originalUserInfo.getName() != null ? originalUserInfo.getName() : "");
                userInfo.setFrom(originalUserInfo.getFrom() != null ? originalUserInfo.getFrom() : 0);
                userInfo.setRealName(originalUserInfo.getRealName() != null ? originalUserInfo.getRealName() : "");
                userInfo.setEmail(originalUserInfo.getEmail() != null ? originalUserInfo.getEmail() : "");
                userInfo.setAvatar(originalUserInfo.getAvatar() != null ? originalUserInfo.getAvatar() : "");
                
                info.setUserInfo(userInfo);
            }
            item.setInfo(info);
        }
        
        // 设置IP地址信息 (模拟数据，实际应该调用IP地址解析服务)
        if (StringUtils.hasText(conversation.getRemoteIp())) {
            IPAddressInfo ipAddress = parseIPAddress(conversation.getRemoteIp());
            item.setIpAddress(ipAddress);
        }
        
        return item;
    }
    
    /**
     * 解析IP地址信息 (模拟实现)
     */
    private IPAddressInfo parseIPAddress(String ip) {
        // 这里应该调用真实的IP地址解析服务
        // 现在返回模拟数据
        return new IPAddressInfo(ip, "中国", "湖北省", "武汉市");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(String id) {
        log.info("删除会话: {}", id);
        
        // 先清理会话状态
        resetConversationState(id);
        
        // 再删除会话记录
        removeById(id);
        
        log.info("会话删除完成: {}", id);
    }
}
