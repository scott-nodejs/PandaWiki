package com.chaitin.pandawiki.dto.stat;

import lombok.Data;

/**
 * 即时访问统计响应
 * 
 * @author chaitin
 */
@Data
public class InstantCountResponse {
    
    /**
     * 时间
     */
    private String time;
    
    /**
     * 访问次数
     */
    private Long count;
} 