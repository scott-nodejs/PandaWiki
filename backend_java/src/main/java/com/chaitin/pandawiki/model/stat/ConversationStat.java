package com.chaitin.pandawiki.model.stat;

import lombok.Data;

import java.time.LocalDate;

/**
 * 会话统计
 */
@Data
public class ConversationStat {
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 会话数
     */
    private Integer conversationCount;
    
    /**
     * 消息数
     */
    private Integer messageCount;
    
    /**
     * 用户数
     */
    private Integer userCount;
    
    /**
     * Token总数
     */
    private Long totalTokens;
    
    /**
     * 平均响应时间(毫秒)
     */
    private Integer avgResponseTime;
} 