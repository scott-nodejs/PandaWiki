package com.chaitin.pandawiki.dto.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 即时页面访问响应
 * 
 * @author chaitin
 */
@Data
public class InstantPageResponse {
    
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
     * 访问时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
} 