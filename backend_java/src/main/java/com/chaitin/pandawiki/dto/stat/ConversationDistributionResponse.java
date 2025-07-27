package com.chaitin.pandawiki.dto.stat;

import lombok.Data;

/**
 * 对话分布响应
 * 
 * @author chaitin
 */
@Data
public class ConversationDistributionResponse {
    
    /**
     * 时间
     */
    private String time;
    
    /**
     * 对话数量
     */
    private Integer count;
} 