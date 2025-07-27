package com.chaitin.pandawiki.model.stat;

import lombok.Data;

import java.time.LocalDate;

/**
 * 节点访问统计
 */
@Data
public class NodeAccessStat {
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 访问次数
     */
    private Integer accessCount;
    
    /**
     * 访问用户数
     */
    private Integer userCount;
    
    /**
     * 平均停留时间(秒)
     */
    private Integer avgDuration;
} 