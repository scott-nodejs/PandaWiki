package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.dto.CreateKnowledgeBaseRequest;
import com.chaitin.pandawiki.entity.KnowledgeBase;
import com.chaitin.pandawiki.model.vo.KnowledgeBaseVo;

/**
 * 知识库服务接口
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 创建知识库
     */
    String createKnowledgeBase(CreateKnowledgeBaseRequest request);

    /**
     * 更新知识库
     */
    void updateKnowledgeBase(CreateKnowledgeBaseRequest request);

    /**
     * 删除知识库
     */
    void deleteKnowledgeBase(String id);

    /**
     * 获取知识库详情
     */
    KnowledgeBaseVo getKnowledgeBaseDetail(String id);
}
