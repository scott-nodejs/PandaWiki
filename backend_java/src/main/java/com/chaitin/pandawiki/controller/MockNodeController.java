package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.dto.node.NodeActionRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟Node服务控制器
 * 用于开发和测试环境，模拟Go服务的node相关接口
 *
 * @author chaitin
 */
@Slf4j
@RestController
@RequestMapping("/mock/api/v1/node")
public class MockNodeController {

    /**
     * 模拟生成节点摘要
     */
    @PostMapping("/summary")
    public Map<String, Object> mockSummary(@RequestBody NodeSummaryRequest request) {
        log.info("模拟生成节点摘要: kbId={}, nodeIds={}", request.getKbId(), request.getIds());

        Map<String, Object> response = new HashMap<>();

        if (request.getIds().size() == 1) {
            // 单个节点，返回摘要内容
            response.put("summary", "这是一个由AI生成的节点摘要示例。该节点包含了重要的信息和内容要点，通过智能分析提取出核心内容，帮助用户快速了解节点的主要内容。");
        } else {
            // 批量处理，返回空字符串（异步处理）
            response.put("summary", "");
        }

        return response;
    }

    /**
     * 模拟执行节点操作
     */
    @PostMapping("/action")
    public Map<String, Object> mockAction(@RequestBody NodeActionRequest request) {
        log.info("模拟执行节点操作: kbId={}, nodeIds={}, action={}",
                request.getKb_id(), request.getIds(), request.getAction());

        // 模拟特殊错误情况
        if ("delete".equals(request.getAction()) && request.getIds().contains("folder-with-children")) {
            throw new RuntimeException("文件夹下有子文件，不能删除~");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作执行成功");

        log.info("模拟节点操作完成: action={}, 处理节点数量={}", request.getAction(), request.getIds().size());

        return response;
    }
}
