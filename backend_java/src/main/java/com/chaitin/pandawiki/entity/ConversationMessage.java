package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息
 * 按照Go后端的ConversationMessage结构设计
 */
@Data
@TableName("conversation_message")
public class ConversationMessage {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 应用ID
     */
    private String appId;

    /**
     * 角色 (user/assistant)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 模型提供商
     */
    private String provider;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 提示词Token数
     */
    private Integer promptTokens = 0;
    
    /**
     * 完成Token数
     */
    private Integer completionTokens = 0;

    /**
     * Token总数
     */
    private Integer totalTokens = 0;
    
    /**
     * 远程IP
     */
    private String remoteIp;

    /**
     * 响应时间(毫秒)
     */
    private Integer responseTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
