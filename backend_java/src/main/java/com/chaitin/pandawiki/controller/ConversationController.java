package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.ConversationListRequest;
import com.chaitin.pandawiki.dto.ConversationListResponse;
import com.chaitin.pandawiki.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 对话管理控制器
 */
@Tag(name = "对话管理", description = "对话管理相关接口")
@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取对话列表
     */
    @Operation(summary = "获取对话列表")
    @GetMapping
    public ApiResponse<ConversationListResponse> getConversationList(
            @RequestParam String kb_id,
            @RequestParam(required = false) String app_id,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String remote_ip,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer per_page) {

        ConversationListRequest request = new ConversationListRequest();
        request.setKbId(kb_id);
        request.setAppId(app_id);
        request.setSubject(subject);
        request.setRemoteIp(remote_ip);
        request.setPage(page);
        request.setPerPage(per_page);

        ConversationListResponse response = conversationService.getConversationList(request);
        return ApiResponse.success(response);
    }
}
