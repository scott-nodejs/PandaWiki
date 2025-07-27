package com.chaitin.pandawiki.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点列表项视图对象
 * 
 * @author chaitin
 */
@Data
public class NodeListItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 节点ID
     */
    private String id;
    
    /**
     * 节点名称
     */
    private String name;
    
    /**
     * 节点类型：1=文档，2=文件夹
     */
    private Integer type;
    
    /**
     * 图标表情
     */
    private String emoji;
    
    /**
     * 摘要
     */
    private String summary;
    
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
     * 状态：1=草稿，2=已完成
     */
    private Integer status;
    
    /**
     * 可见性：1=私有，2=公开
     */
    private Integer visibility;
    
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 子节点列表
     */
    private List<NodeListItemVO> children;
} 