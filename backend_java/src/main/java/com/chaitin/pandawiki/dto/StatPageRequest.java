package com.chaitin.pandawiki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 页面访问统计请求
 * 
 * @author chaitin
 */
@Data
public class StatPageRequest {
    
    /**
     * 节点ID
     */
    @JsonProperty("node_id")
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;
    
    /**
     * 场景类型
     * 1=欢迎页面
     * 2=节点页面  
     * 3=聊天页面
     * 4=认证页面
     */
    @NotNull(message = "场景类型不能为空")
    private Integer scene;
} 