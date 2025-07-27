package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点实体
 */
@Data
@TableName("node")
public class Node {

    @TableId
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点内容
     */
    private String content;

    /**
     * 节点类型（1=文件夹, 2=文档）
     */
    private Integer type;

    /**
     * 节点状态（1=处理中, 2=已完成）
     */
    private Integer status;

    /**
     * 可见性（1=私有, 2=公开）
     */
    private Integer visibility;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 表情符号
     */
    private String emoji;

    /**
     * 位置/排序位置
     */
    private Double position;

    /**
     * 父节点ID
     */
    @JsonProperty("parent_id")
    private String parentId;

    /**
     * 知识库ID
     */
    @JsonProperty("kb_id")
    private String kbId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建者ID
     */
    @JsonProperty("created_by")
    private String createdBy;
}
