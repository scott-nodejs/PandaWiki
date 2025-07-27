package com.chaitin.pandawiki.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.dto.CreateKnowledgeBaseRequest;
import com.chaitin.pandawiki.entity.KnowledgeBase;
import com.chaitin.pandawiki.mapper.KnowledgeBaseMapper;
import com.chaitin.pandawiki.model.vo.KnowledgeBaseVo;
import com.chaitin.pandawiki.service.KnowledgeBaseService;
import com.chaitin.pandawiki.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 知识库服务实现类
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        // 检查端口和主机是否已被占用
        // TODO: 实现端口和主机占用检查逻辑

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setId(UUID.randomUUID().toString().replace("-", ""));
        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBase.setOwnerId(userService.getCurrentUser().getId());

        this.save(knowledgeBase);
        return knowledgeBase.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(CreateKnowledgeBaseRequest request) {
        KnowledgeBase knowledgeBase = this.getById(request.getId());
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }

        String jsonString = JSONObject.toJSONString(request.getAccess_settings());
        knowledgeBase.setAccessSettings(jsonString);
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        this.updateById(knowledgeBase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(String id) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }

        // TODO: 清理相关数据（节点、文件等）

        this.removeById(id);
    }

    @Override
    public KnowledgeBaseVo getKnowledgeBaseDetail(String id) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }
        KnowledgeBaseVo knowledgeBaseVo = new KnowledgeBaseVo();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVo);
        String accessSettings = knowledgeBase.getAccessSettings();
        JSONObject jsonObject = JSONObject.parseObject(accessSettings);
        knowledgeBaseVo.setAccess_settings(jsonObject);
        return knowledgeBaseVo;
    }
}
