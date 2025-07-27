package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.entity.Conversation;
import com.chaitin.pandawiki.dto.ConversationListRequest;
import com.chaitin.pandawiki.dto.ConversationListResponse;

/**
 * 会话服务接口
 */
public interface ConversationService extends IService<Conversation> {
    
    /**
     * 创建会话
     *
     * @param conversation 会话信息
     * @return 会话ID
     */
    String createConversation(Conversation conversation);
    
    /**
     * 分页查询会话列表
     *
     * @param kbId     知识库ID
     * @param appId    应用ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 会话列表
     */
    Page<Conversation> pageConversations(String kbId, String appId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取对话列表
     *
     * @param request 查询请求
     * @return 对话列表响应
     */
    ConversationListResponse getConversationList(ConversationListRequest request);
    
    /**
     * 删除会话
     *
     * @param id 会话ID
     */
    void deleteConversation(String id);
} 