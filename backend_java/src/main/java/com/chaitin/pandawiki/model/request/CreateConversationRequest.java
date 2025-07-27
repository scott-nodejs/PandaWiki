package com.chaitin.pandawiki.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建会话请求
 */
@Data
@Schema(description = "创建会话请求")
public class CreateConversationRequest {
    /**
     * 知识库ID
     */
    @Schema(description = "知识库ID", required = true)
    private String kbId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", required = true)
    private String userId;

    /**
     * 会话标题
     */
    @Schema(description = "会话标题")
    private String title;

    /**
     * 初始消息
     */
    @Schema(description = "初始消息")
    private String initialMessage;
}
