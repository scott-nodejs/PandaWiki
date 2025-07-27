package com.chaitin.pandawiki.dto.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 热门页面响应
 * 
 * @author chaitin
 */
@Data
public class HotPageResponse {
    
    /**
     * 场景类型
     */
    private Integer scene;
    
    /**
     * 节点ID
     */
    @JsonProperty("node_id")
    private String nodeId;
    
    /**
     * 节点名称
     */
    @JsonProperty("node_name")
    private String nodeName;
    
    /**
     * 访问次数
     */
    private Integer count;
} 