package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaitin.pandawiki.dto.CreateReleaseRequest;
import com.chaitin.pandawiki.dto.GetReleaseListRequest;
import com.chaitin.pandawiki.dto.ReleaseListResponse;
import com.chaitin.pandawiki.entity.Release;
import com.chaitin.pandawiki.entity.ReleaseNode;
import com.chaitin.pandawiki.mapper.ReleaseMapper;
import com.chaitin.pandawiki.mapper.ReleaseNodeMapper;
import com.chaitin.pandawiki.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 发布版本服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseServiceImpl implements ReleaseService {

    private final ReleaseMapper releaseMapper;
    private final ReleaseNodeMapper releaseNodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRelease(CreateReleaseRequest request) {
        log.info("创建发布版本: kbId={}, tag={}, nodeIds={}",
            request.getKb_id(), request.getTag(), request.getNode_ids());

        // 创建发布记录
        Release release = new Release();
        release.setId(UUID.randomUUID().toString().replace("-", ""));
        release.setKbId(request.getKb_id());
        release.setTag(request.getTag());
        release.setMessage(request.getMessage());
        release.setPublishTime(LocalDateTime.now());
        release.setStatus(1); // 已发布
        release.setCreateTime(LocalDateTime.now());
        release.setUpdateTime(LocalDateTime.now());

        releaseMapper.insert(release);

        // 创建发布节点关联记录
        List<ReleaseNode> releaseNodes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String nodeId : request.getNode_ids()) {
            ReleaseNode releaseNode = new ReleaseNode();
            releaseNode.setId(UUID.randomUUID().toString().replace("-", ""));
            releaseNode.setReleaseId(release.getId());
            releaseNode.setNodeId(nodeId);
            releaseNode.setCreateTime(now);
            releaseNodes.add(releaseNode);
        }

        // 批量插入关联记录
        for (ReleaseNode releaseNode : releaseNodes) {
            releaseNodeMapper.insert(releaseNode);
        }

        log.info("发布版本创建成功: releaseId={}", release.getId());
        return release.getId();
    }
    
    @Override
    public ReleaseListResponse getReleaseList(GetReleaseListRequest request) {
        log.info("获取发布列表: kbId={}, page={}, perPage={}", 
            request.getKbId(), request.getPage(), request.getPerPage());
        
        // 分页查询发布记录
        Page<Release> page = new Page<>(request.getPage(), request.getPerPage());
        QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("kb_id", request.getKbId())
                   .eq("status", 1) // 只查询已发布的
                   .orderByDesc("publish_time");
        
        IPage<Release> releasePages = releaseMapper.selectPage(page, queryWrapper);
        
        // 转换为响应对象
        List<ReleaseListResponse.ReleaseItem> releaseItems = new ArrayList<>();
        
        for (Release release : releasePages.getRecords()) {
            ReleaseListResponse.ReleaseItem item = new ReleaseListResponse.ReleaseItem();
            item.setId(release.getId());
            item.setKbId(release.getKbId());
            item.setTag(release.getTag());
            item.setMessage(release.getMessage());
            item.setPublishTime(release.getPublishTime());
            item.setStatus(release.getStatus());
            
            // 查询该发布版本的节点ID列表
            QueryWrapper<ReleaseNode> nodeQuery = new QueryWrapper<>();
            nodeQuery.eq("release_id", release.getId());
            List<ReleaseNode> releaseNodes = releaseNodeMapper.selectList(nodeQuery);
            List<String> nodeIds = releaseNodes.stream()
                .map(ReleaseNode::getNodeId)
                .collect(Collectors.toList());
            item.setNodeIds(nodeIds);
            
            releaseItems.add(item);
        }
        
        ReleaseListResponse response = new ReleaseListResponse();
        response.setData(releaseItems);
        response.setTotal(releasePages.getTotal());
        
        log.info("获取发布列表成功: total={}", releasePages.getTotal());
        return response;
    }
}
