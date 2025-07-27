package com.chaitin.pandawiki.dto.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 总计统计响应
 * 
 * @author chaitin
 */
@Data
public class CountStatsResponse {
    
    /**
     * IP数量
     */
    @JsonProperty("ip_count")
    private Long ipCount;
    
    /**
     * 会话数量
     */
    @JsonProperty("session_count")
    private Long sessionCount;
    
    /**
     * 页面访问数量
     */
    @JsonProperty("page_visit_count")
    private Long pageVisitCount;
    
    /**
     * 对话数量
     */
    @JsonProperty("conversation_count")
    private Long conversationCount;
} 