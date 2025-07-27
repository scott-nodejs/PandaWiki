package com.chaitin.pandawiki.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新会话请求
 */
@Data
@Schema(description = "更新会话请求")
public class UpdateConversationRequest {
    /**
     * 会话标题
     */
    @Schema(description = "会话标题")
    private String title;
} 