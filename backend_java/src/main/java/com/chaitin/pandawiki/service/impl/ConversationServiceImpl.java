package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.entity.Conversation;
import com.chaitin.pandawiki.mapper.ConversationMapper;
import com.chaitin.pandawiki.service.ConversationService;
import com.chaitin.pandawiki.dto.ConversationListRequest;
import com.chaitin.pandawiki.dto.ConversationListResponse;
import com.chaitin.pandawiki.dto.IPAddressInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

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
        log.info("获取对话列表: kbId={}, page={}, perPage={}", 
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
        removeById(id);
    }
}
