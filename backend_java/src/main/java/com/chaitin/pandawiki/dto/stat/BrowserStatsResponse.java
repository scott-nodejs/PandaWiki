package com.chaitin.pandawiki.dto.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 浏览器和操作系统统计响应
 * 
 * @author chaitin
 */
@Data
public class BrowserStatsResponse {
    
    /**
     * 浏览器统计列表
     */
    @JsonProperty("browser")
    private List<TrendData> browser;
    
    /**
     * 操作系统统计列表
     */
    @JsonProperty("os")
    private List<TrendData> os;
} 