package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布节点关联实体
 */
@Data
@TableName("release_node")
public class ReleaseNode {
    
    /**
     * 主键ID
     */
    @TableId
    private String id;
    
    /**
     * 发布版本ID
     */
    private String releaseId;
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 