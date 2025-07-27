package com.chaitin.pandawiki.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

/**
 * 对话列表查询请求
 */
@Data
public class ConversationListRequest {
    
    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String kbId;
    
    /**
     * 应用ID (可选)
     */
    private String appId;
    
    /**
     * 主题关键词 (可选)
     */
    private String subject;
    
    /**
     * 远程IP (可选)
     */
    private String remoteIp;
    
    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer perPage = 20;
} 