package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.node.NodeActionRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryResponse;
import com.chaitin.pandawiki.entity.Node;
import com.chaitin.pandawiki.service.NodeExtendedService;
import com.chaitin.pandawiki.service.NodeService;
import com.chaitin.pandawiki.service.ai.assistant.IAssistant.SummarizeAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点扩展服务实现
 * 修复版：直接操作数据库，不再调用外部 HTTP 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeExtendedServiceImpl implements NodeExtendedService {

    private final SummarizeAssistant summarizeAssistant;
    private final RestTemplate restTemplate;
    private final CrawlerConfig crawlerConfig;

    @Autowired
    private NodeService nodeService;

    @Override
    public NodeSummaryResponse generateSummary(NodeSummaryRequest request) {
        log.info("生成节点摘要: nodeIds={}", request.getIds());

        try {
            StringBuilder contentBuilder = new StringBuilder();
            int processedCount = 0;

            // 获取所有节点内容
            for (String nodeId : request.getIds()) {
                try {
                    Node node = nodeService.getNodeDetail(nodeId);
                    if (node != null && node.getContent() != null && !node.getContent().trim().isEmpty()) {
                        contentBuilder.append("节点[").append(node.getName()).append("]: ");
                        contentBuilder.append(node.getContent()).append("\n\n");
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.warn("获取节点内容失败: nodeId={}, error={}", nodeId, e.getMessage());
                }
            }

            if (contentBuilder.length() == 0) {
                throw new RuntimeException("未找到有效的节点内容用于生成摘要");
            }

            // 调用AI生成摘要
            String summary = summarizeAssistant.summarize(contentBuilder.toString());

            NodeSummaryResponse response = new NodeSummaryResponse();
            response.setSummary(summary);

            log.info("节点摘要生成成功: processedCount={}, summaryLength={}", 
                    processedCount, summary.length());

            return response;

        } catch (Exception e) {
            log.error("生成节点摘要失败", e);
            throw new RuntimeException("生成节点摘要失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeAction(NodeActionRequest request) {
        log.info("执行节点操作: kbId={}, nodeIds={}, action={}",
                request.getKb_id(), request.getIds(), request.getAction());

        try {
            switch (request.getAction()) {
                case "delete":
                    executeDeleteAction(request.getIds());
                    break;
                case "private":
                    executeVisibilityAction(request.getIds(), 1); // 1=私有
                    break;
                case "public":
                    executeVisibilityAction(request.getIds(), 2); // 2=公开
                    break;
                default:
                    throw new RuntimeException("不支持的操作类型: " + request.getAction());
            }

            log.info("节点操作执行成功: action={}, nodeCount={}",
                    request.getAction(), request.getIds().size());

        } catch (Exception e) {
            log.error("执行节点操作失败", e);

            // 特殊错误处理
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("文件夹下有子文件")) {
                throw new RuntimeException("文件夹下有子文件，不能删除");
            }

            throw new RuntimeException("执行节点操作失败: " + e.getMessage());
        }
    }

    /**
     * 执行删除操作
     */
    private void executeDeleteAction(List<String> nodeIds) {
        log.info("执行删除操作，节点数量: {}", nodeIds.size());
        
        for (String nodeId : nodeIds) {
            try {
                nodeService.deleteNode(nodeId);
                log.debug("节点删除成功: {}", nodeId);
            } catch (Exception e) {
                log.error("删除节点失败: nodeId={}, error={}", nodeId, e.getMessage());
                // 如果是"文件夹下有子文件"的错误，直接抛出
                if (e.getMessage() != null && e.getMessage().contains("文件夹下有子文件")) {
                    throw e;
                }
                // 其他错误继续处理其他节点，但记录错误
                log.warn("跳过删除失败的节点: {}", nodeId);
            }
        }
    }

    /**
     * 执行可见性变更操作
     */
    private void executeVisibilityAction(List<String> nodeIds, int visibility) {
        String visibilityName = (visibility == 1) ? "私有" : "公开";
        log.info("执行{}操作，节点数量: {}", visibilityName, nodeIds.size());

        for (String nodeId : nodeIds) {
            try {
                Node node = nodeService.getNodeDetail(nodeId);
                if (node != null) {
                    node.setVisibility(visibility);
                    node.setUpdatedAt(LocalDateTime.now());
                    nodeService.updateById(node);
                    log.debug("节点可见性更新成功: nodeId={}, visibility={}", nodeId, visibilityName);
                } else {
                    log.warn("节点不存在，跳过: {}", nodeId);
                }
            } catch (Exception e) {
                log.error("更新节点可见性失败: nodeId={}, error={}", nodeId, e.getMessage());
                // 继续处理其他节点
            }
        }
    }
}
