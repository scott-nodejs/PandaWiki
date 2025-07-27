package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.CreateNodeDetailRequest;
import com.chaitin.pandawiki.dto.CreateNodeRequest;
import com.chaitin.pandawiki.dto.node.NodeActionRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryRequest;
import com.chaitin.pandawiki.dto.node.NodeSummaryResponse;
import com.chaitin.pandawiki.entity.Node;
import com.chaitin.pandawiki.model.vo.NodeWithRecommendationsVO;
import com.chaitin.pandawiki.service.NodeExtendedService;
import com.chaitin.pandawiki.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点管理控制器
 */
@Tag(name = "节点管理", description = "节点管理相关接口")
@RestController
@RequestMapping("/api/v1/node")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService nodeService;
    private final NodeExtendedService nodeExtendedService;

    /**
     * 创建节点
     */
    @Operation(summary = "创建节点")
    @PostMapping("")
    public ApiResponse<Map<String, String>> createNode(@Validated @RequestBody CreateNodeRequest request) {
        String id = nodeService.createNode(request);
        Map<String, String> result = new HashMap<>();
        result.put("id", id);
        return ApiResponse.success(result);
    }

    /**
     * 获取节点列表
     */
    @Operation(summary = "获取节点列表")
    @GetMapping("/list")
    public ApiResponse<List<Node>> getNodeList(
            @RequestParam String kb_id,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false, defaultValue = "true") boolean all) {

        List<Node> nodes;
        if (all) {
            // 获取知识库的所有节点
            nodes = nodeService.getAllNodesByKbId(kb_id);
        } else {
            // 获取特定父节点下的节点
            nodes = nodeService.getNodeList(kb_id, parentId);
        }

        return ApiResponse.success(nodes);
    }

    /**
     * 获取节点详情
     */
    @Operation(summary = "获取节点详情")
    @GetMapping("/detail")
    public ApiResponse<Node> getNodeDetail(@RequestParam String id) {
        Node node = nodeService.getNodeDetail(id);
        return ApiResponse.success(node);
    }

    /**
     * 更新节点
     */
    @Operation(summary = "更新节点")
    @PutMapping("/detail")
    public ApiResponse<Void> updateNode(
            @RequestBody CreateNodeDetailRequest content) {
        nodeService.updateNode(content);
        return ApiResponse.success();
    }

    /**
     * 删除节点
     */
    @Operation(summary = "删除节点")
    @DeleteMapping("/detail")
    public ApiResponse<Void> deleteNode(@RequestParam String id) {
        nodeService.deleteNode(id);
        return ApiResponse.success();
    }

    /**
     * 移动节点
     */
    @Operation(summary = "移动节点")
    @PostMapping("/move")
    public ApiResponse<Void> moveNode(
            @RequestParam String nodeId,
            @RequestParam(required = false) String targetParentId) {
        nodeService.moveNode(nodeId, targetParentId);
        return ApiResponse.success();
    }

    /**
     * 批量移动节点
     */
    @Operation(summary = "批量移动节点")
    @PostMapping("/batch_move")
    public ApiResponse<Void> batchMoveNodes(
            @RequestParam List<String> nodeIds,
            @RequestParam(required = false) String targetParentId) {
        nodeService.batchMoveNodes(nodeIds, targetParentId);
        return ApiResponse.success();
    }

    /**
     * 推荐节点
     */
    @Operation(summary = "推荐节点")
    @GetMapping("/recommend_nodes")
    public ApiResponse<List<NodeWithRecommendationsVO>> recommendNodes(
            @RequestParam String kb_id,
            @RequestParam("node_ids") List<String> nodeIds) {
        List<NodeWithRecommendationsVO> recommendedNodes = nodeService.recommendNodes(kb_id, nodeIds);
        return ApiResponse.success(recommendedNodes);
    }

    // ==== 扩展接口 ====

    /**
     * 生成节点摘要
     */
    @Operation(summary = "生成节点摘要", description = "基于节点内容生成AI摘要，支持单个或批量处理")
    @PostMapping("/summary")
    public ApiResponse<NodeSummaryResponse> generateSummary(@Valid @RequestBody NodeSummaryRequest request) {
        try {
            NodeSummaryResponse response = nodeExtendedService.generateSummary(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            if (e.getMessage().contains("请前往管理后台")) {
                return ApiResponse.error("请前往管理后台，点击右上角的\"系统设置\"配置推理大模型。");
            }
            return ApiResponse.error("生成节点摘要失败: " + e.getMessage());
        }
    }

    /**
     * 执行节点操作
     */
    @Operation(summary = "执行节点操作", description = "对节点执行批量操作：删除、设为私有、设为公开")
    @PostMapping("/action")
    public ApiResponse<Void> executeAction(@Valid @RequestBody NodeActionRequest request) {
        try {
            nodeExtendedService.executeAction(request);
            return ApiResponse.success();
        } catch (Exception e) {
            if (e.getMessage().contains("文件夹下有子文件")) {
                return ApiResponse.error("文件夹下有子文件，不能删除~");
            }
            return ApiResponse.error("执行节点操作失败: " + e.getMessage());
        }
    }
}
