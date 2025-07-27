package com.chaitin.pandawiki.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建节点请求DTO
 */
@Data
public class CreateNodeRequest {

    /**
     * 节点名称
     */
    @NotBlank(message = "节点名称不能为空")
    private String name;

    /**
     * 节点内容
     */
    private String content;

    /**
     * 表情符号
     */
    private String emoji;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 节点类型 (1=文件夹, 2=文档)
     */
    @NotNull(message = "节点类型不能为空")
    private Integer type;

    /**
     * 节点状态 (1=处理中, 2=已完成)
     */
    private Integer status;

    /**
     * 可见性 (1=私有, 2=公开)
     */
    private Integer visibility;

    /**
     * 位置/排序位置
     */
    private Double position;

    /**
     * 父节点ID
     */
    private String parent_id;

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String kb_id;

    /**
     * 排序
     */
    private Integer sort;
}
