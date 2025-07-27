package com.chaitin.pandawiki.service.impl;

import com.chaitin.pandawiki.config.CrawlerConfig;
import com.chaitin.pandawiki.dto.node.NodeActionRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryResponse;
import com.chaitin.pandawiki.service.NodeExtendedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 扩展的节点服务实现
 *
 * @author chaitin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeExtendedServiceImpl implements NodeExtendedService {

    private final RestTemplate restTemplate;
    private final CrawlerConfig crawlerConfig;

    @Override
    public NodeSummaryResponse generateSummary(NodeSummaryRequest request) {
        log.info("生成节点摘要: kbId={}, nodeIds={}", request.getKbId(), request.getIds());

        try {
            String url = crawlerConfig.getServiceUrl() + "/node/summary";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NodeSummaryRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("summary")) {
                NodeSummaryResponse summaryResponse = new NodeSummaryResponse();
                summaryResponse.setSummary((String) responseBody.get("summary"));
                return summaryResponse;
            }

            // 如果没有返回摘要内容，说明是批量处理，返回空摘要
            NodeSummaryResponse summaryResponse = new NodeSummaryResponse();
            summaryResponse.setSummary("");
            return summaryResponse;

        } catch (Exception e) {
            log.error("生成节点摘要失败", e);
            throw new RuntimeException("生成节点摘要失败: " + e.getMessage());
        }
    }

    @Override
    public void executeAction(NodeActionRequest request) {
        log.info("执行节点操作: kbId={}, nodeIds={}, action={}", 
                request.getKbId(), request.getIds(), request.getAction());

        try {
            String url = crawlerConfig.getServiceUrl() + "/node/action";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NodeActionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("节点操作执行成功: action={}, nodeCount={}", 
                        request.getAction(), request.getIds().size());
            } else {
                throw new RuntimeException("节点操作执行失败，状态码: " + response.getStatusCode());
            }

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
} 