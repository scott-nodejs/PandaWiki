package com.chaitin.pandawiki.model;

import lombok.Data;

/**
 * 节点发布信息
 */
@Data
public class NodeRelease {
    private String id;
    private String nodeId;
    private String content;
    private String title;
    private String summary;
} 