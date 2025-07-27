package com.chaitin.pandawiki.controller.share;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.model.vo.NodeDetailVO;
import com.chaitin.pandawiki.model.vo.NodeListItemVO;
import com.chaitin.pandawiki.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Share节点接口控制器
 * 
 * @author chaitin
 */
@Tag(name = "Share节点接口", description = "分享节点相关接口")
@RestController
@RequestMapping("/share/v1/node")
@RequiredArgsConstructor
@Slf4j
public class ShareNodeController {
    
    private final NodeService nodeService;
    
    /**
     * 获取节点列表
     */
    @Operation(summary = "获取节点列表", description = "根据知识库ID获取已发布的节点列表")
    @GetMapping("/list")
    public ApiResponse<List<NodeListItemVO>> getNodeList(
            @Parameter(description = "知识库ID", required = true)
            @RequestHeader("X-KB-ID") String kbId) {
        
        log.info("获取节点列表, kbId: {}", kbId);
        
        if (kbId == null || kbId.trim().isEmpty()) {
            return ApiResponse.error("知识库ID不能为空");
        }
        
        try {
            List<NodeListItemVO> nodeList = nodeService.getPublishedNodeList(kbId);
            return ApiResponse.success(nodeList);
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            return ApiResponse.error("获取节点列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点详情
     */
    @Operation(summary = "获取节点详情", description = "根据知识库ID和节点ID获取节点详细信息")
    @GetMapping("/detail")
    public ApiResponse<NodeDetailVO> getNodeDetail(
            @Parameter(description = "知识库ID", required = true)
            @RequestHeader("X-KB-ID") String kbId,
            @Parameter(description = "节点ID", required = true)
            @RequestParam("id") String nodeId) {
        
        log.info("获取节点详情, kbId: {}, nodeId: {}", kbId, nodeId);
        
        if (kbId == null || kbId.trim().isEmpty()) {
            return ApiResponse.error("知识库ID不能为空");
        }
        
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return ApiResponse.error("节点ID不能为空");
        }
        
        try {
            NodeDetailVO nodeDetail = nodeService.getPublishedNodeDetail(kbId, nodeId);
            if (nodeDetail == null) {
                return ApiResponse.error("节点不存在或未发布");
            }
            return ApiResponse.success(nodeDetail);
        } catch (Exception e) {
            log.error("获取节点详情失败", e);
            return ApiResponse.error("获取节点详情失败: " + e.getMessage());
        }
    }
} 