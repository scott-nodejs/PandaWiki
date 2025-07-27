package com.chaitin.pandawiki.model.stat;

import lombok.Data;

import java.time.LocalDate;

/**
 * 模型使用统计
 */
@Data
public class ModelUsageStat {
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 模型ID
     */
    private String modelId;
    
    /**
     * 调用次数
     */
    private Integer callCount;
    
    /**
     * Token总数
     */
    private Long totalTokens;
    
    /**
     * 平均响应时间(毫秒)
     */
    private Integer avgResponseTime;
    
    /**
     * 成功率
     */
    private Double successRate;
} 