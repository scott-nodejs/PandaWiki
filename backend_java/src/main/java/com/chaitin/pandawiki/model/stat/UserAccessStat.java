package com.chaitin.pandawiki.model.stat;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户访问统计
 */
@Data
public class UserAccessStat {
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 访问次数
     */
    private Integer accessCount;
    
    /**
     * 访问节点数
     */
    private Integer nodeCount;
    
    /**
     * 总停留时间(秒)
     */
    private Integer totalDuration;
} 