package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaitin.pandawiki.dto.QAListRequest;
import com.chaitin.pandawiki.dto.QAListResponse;

/**
 * 问答服务接口
 * 
 * @author chaitin
 */
public interface QAService {
    
    /**
     * 获取问答对列表
     * 
     * @param request 查询请求
     * @return 问答列表响应
     */
    QAListResponse getQAList(QAListRequest request);
    
    /**
     * 根据会话ID获取问答记录
     * 
     * @param conversationId 会话ID
     * @return 问答记录列表
     */
    QAListResponse getQAByConversationId(String conversationId);
    
    /**
     * 删除问答记录
     * 
     * @param conversationId 会话ID
     */
    void deleteQAByConversationId(String conversationId);
    
    /**
     * 搜索问答记录
     * 
     * @param kbId 知识库ID
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    QAListResponse searchQA(String kbId, String keyword, Integer pageNum, Integer pageSize);
} 