package com.chaitin.pandawiki.dto.stat;

import lombok.Data;

/**
 * 趋势数据
 * 
 * @author chaitin
 */
@Data
public class TrendData {
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 数量
     */
    private Integer count;
} 