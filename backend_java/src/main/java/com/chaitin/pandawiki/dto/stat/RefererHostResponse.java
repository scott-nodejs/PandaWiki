package com.chaitin.pandawiki.dto.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 来源站点响应
 * 
 * @author chaitin
 */
@Data
public class RefererHostResponse {
    
    /**
     * 来源站点
     */
    @JsonProperty("referer_host")
    private String refererHost;
    
    /**
     * 访问次数
     */
    private Integer count;
} 