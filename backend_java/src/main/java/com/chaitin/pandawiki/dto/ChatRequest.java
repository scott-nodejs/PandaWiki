package com.chaitin.pandawiki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 聊天请求
 * 
 * @author chaitin
 */
@Data
public class ChatRequest {
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息不能为空")
    private String message;
    
    /**
     * 安全令牌
     */
    private String nonce = "";
    
    /**
     * 对话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId = "";
    
    /**
     * 应用类型：1=Web应用，2=Widget应用
     */
    @JsonProperty("app_type")
    @NotNull(message = "应用类型不能为空")
    private Integer appType;
    
    /**
     * 知识库ID（从Header获取）
     */
    private String kbId;
    
    /**
     * 应用ID（内部使用）
     */
    private String appId;
    
    /**
     * 客户端IP（内部使用）
     */
    private String remoteIp;
    
    /**
     * 对话信息（内部使用）
     */
    private Object info;
} 