package com.chaitin.pandawiki.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QA列表响应DTO
 * 
 * @author chaitin
 */
@Data
public class QAListResponse {
    
    /**
     * QA对列表
     */
    private List<QAItem> data;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * QA项
     */
    @Data
    public static class QAItem {
        
        /**
         * 会话ID
         */
        private String conversationId;
        
        /**
         * 会话标题
         */
        private String subject;
        
        /**
         * 用户问题
         */
        private String question;
        
        /**
         * AI回答
         */
        private String answer;
        
        /**
         * 响应时间(毫秒)
         */
        private Integer responseTime;
        
        /**
         * Token使用量
         */
        private Long totalTokens;
        
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        
        /**
         * 知识库ID
         */
        private String kbId;
    }
} 