package com.chaitin.pandawiki.model.stat;

import lombok.Data;

/**
 * 知识库统计
 */
@Data
public class KnowledgeBaseStat {
    /**
     * 知识库ID
     */
    private String kbId;
    
    /**
     * 文档数量
     */
    private Integer documentCount;
    
    /**
     * 文件夹数量
     */
    private Integer folderCount;
    
    /**
     * 访问次数
     */
    private Integer accessCount;
    
    /**
     * 访问用户数
     */
    private Integer userCount;
    
    /**
     * 会话数量
     */
    private Integer conversationCount;
    
    /**
     * Token总数
     */
    private Long tokenCount;
} 