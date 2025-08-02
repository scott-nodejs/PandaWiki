package com.chaitin.pandawiki.controller.admin;

import com.chaitin.pandawiki.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话管理控制器
 * 提供会话重置和异常清理功能，用于解决 QwenHelper 消息处理错误
 * 
 * @author chaitin
 */
@Tag(name = "会话管理", description = "会话状态管理和异常清理 API")
@RestController
@RequestMapping("/api/admin/conversation")
@RequiredArgsConstructor
@Slf4j
public class ConversationManagementController {
    
    private final ConversationService conversationService;
    
    @Operation(summary = "重置会话状态", description = "完全重置会话状态，清理所有消息和缓存")
    @PostMapping("/{conversationId}/reset")
    public ResponseEntity<Map<String, Object>> resetConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId) {
        
        log.info("收到会话重置请求: {}", conversationId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = conversationService.resetConversationState(conversationId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "会话状态重置成功");
                response.put("conversationId", conversationId);
                log.info("会话重置成功: {}", conversationId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "会话状态重置失败");
                response.put("conversationId", conversationId);
                log.warn("会话重置失败: {}", conversationId);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("会话重置异常: {}", conversationId, e);
            response.put("success", false);
            response.put("message", "重置过程中发生异常: " + e.getMessage());
            response.put("conversationId", conversationId);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "清理会话异常消息", description = "清理重复消息、空消息等异常数据")
    @PostMapping("/{conversationId}/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId) {
        
        log.info("收到会话清理请求: {}", conversationId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int cleanedCount = conversationService.cleanupCorruptedMessages(conversationId);
            
            response.put("success", true);
            response.put("message", "异常消息清理完成");
            response.put("conversationId", conversationId);
            response.put("cleanedCount", cleanedCount);
            
            log.info("会话清理完成: {}, 清理数量: {}", conversationId, cleanedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("会话清理异常: {}", conversationId, e);
            response.put("success", false);
            response.put("message", "清理过程中发生异常: " + e.getMessage());
            response.put("conversationId", conversationId);
            response.put("cleanedCount", 0);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "批量重置会话状态", description = "批量重置多个会话的状态")
    @PostMapping("/batch-reset")
    public ResponseEntity<Map<String, Object>> batchResetConversations(
            @Parameter(description = "会话ID列表", required = true)
            @RequestBody BatchResetRequest request) {
        
        log.info("收到批量会话重置请求, 数量: {}", request.getConversationIds().size());
        
        Map<String, Object> response = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        
        try {
            for (String conversationId : request.getConversationIds()) {
                try {
                    boolean success = conversationService.resetConversationState(conversationId);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("批量重置中单个会话失败: {}", conversationId, e);
                    failureCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", "批量重置完成");
            response.put("total", request.getConversationIds().size());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            
            log.info("批量会话重置完成 - 成功: {}, 失败: {}", successCount, failureCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量会话重置异常", e);
            response.put("success", false);
            response.put("message", "批量重置过程中发生异常: " + e.getMessage());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "会话状态健康检查", description = "检查会话状态是否存在异常")
    @GetMapping("/{conversationId}/health")
    public ResponseEntity<Map<String, Object>> checkConversationHealth(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId) {
        
        log.info("收到会话健康检查请求: {}", conversationId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 简单的健康检查 - 清理异常消息但不删除正常消息
            int issueCount = conversationService.cleanupCorruptedMessages(conversationId);
            
            response.put("conversationId", conversationId);
            response.put("issuesFound", issueCount);
            response.put("status", issueCount > 0 ? "需要清理" : "健康");
            response.put("recommendation", issueCount > 0 ? 
                "建议执行清理操作" : "会话状态正常");
            
            log.info("会话健康检查完成: {}, 发现问题数量: {}", conversationId, issueCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("会话健康检查异常: {}", conversationId, e);
            response.put("conversationId", conversationId);
            response.put("status", "检查失败");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 批量重置请求对象
     */
    public static class BatchResetRequest {
        private java.util.List<String> conversationIds;
        
        public java.util.List<String> getConversationIds() {
            return conversationIds;
        }
        
        public void setConversationIds(java.util.List<String> conversationIds) {
            this.conversationIds = conversationIds;
        }
    }
} 