package com.chaitin.pandawiki.dto;

import lombok.Data;

/**
 * QA列表查询请求DTO
 * 
 * @author chaitin
 */
@Data
public class QAListRequest {
    
    /**
     * 知识库ID
     */
    private String kbId;
    
    /**
     * 会话ID（可选）
     */
    private String conversationId;
    
    /**
     * 搜索关键词（可选）
     */
    private String keyword;
    
    /**
     * 页码（默认1）
     */
    private Integer page = 1;
    
    /**
     * 每页大小（默认10）
     */
    private Integer perPage = 10;
    
    /**
     * 开始时间（可选）
     */
    private String startTime;
    
    /**
     * 结束时间（可选）
     */
    private String endTime;
} 