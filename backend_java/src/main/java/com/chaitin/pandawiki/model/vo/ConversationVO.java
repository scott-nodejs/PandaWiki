package com.chaitin.pandawiki.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话VO
 */
@Data
public class ConversationVO {
    /**
     * 会话ID
     */
    private String id;
    
    /**
     * 知识库ID
     */
    private String kbId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 消息列表
     */
    private List<MessageVO> messages;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 消息VO
     */
    @Data
    public static class MessageVO {
        /**
         * 消息ID
         */
        private String id;
        
        /**
         * 角色
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
        
        /**
         * Token数量
         */
        private Long totalTokens;
        
        /**
         * 响应时间(毫秒)
         */
        private Integer responseTime;
        
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }
} 