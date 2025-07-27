package com.chaitin.pandawiki.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建节点请求DTO
 */
@Data
public class CreateNodeDetailRequest {

    /**
     * 节点名称
     */
    @NotBlank(message = "节点名称不能为空")
    private String id;

    /**
     * 节点内容
     */
    @NotBlank(message = "节点名称不能为空")
    private String content;

    @NotBlank(message = "知识库id")
    private String kb_id;
}
