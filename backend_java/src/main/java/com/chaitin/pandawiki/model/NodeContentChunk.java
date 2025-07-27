package com.chaitin.pandawiki.model;

import lombok.Data;

import java.util.List;

/**
 * 节点内容块
 */
@Data
public class NodeContentChunk {
    private String id;
    private String content;
    private String documentId;
    private String datasetId;
    private List<String> importantKeywords;
    private List<String> questions;
    private Boolean available;
    private String createTime;
    private Double createTimestamp;
} 