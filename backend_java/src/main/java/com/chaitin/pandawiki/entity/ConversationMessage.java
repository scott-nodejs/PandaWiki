package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息
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
     * 角色
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * Token总数
     */
    private Long totalTokens;
    
    /**
     * 响应时间(毫秒)
     */
    private Integer responseTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 