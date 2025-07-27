package com.chaitin.pandawiki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话列表响应
 */
@Data
public class ConversationListResponse {
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 对话列表数据
     */
    private List<ConversationItem> data;
    
    /**
     * 对话列表项
     */
    @Data
    public static class ConversationItem {
        
        /**
         * 对话ID
         */
        private String id;
        
        /**
         * 应用名称
         */
        @JsonProperty("app_name")
        private String appName;
        
        /**
         * 用户信息
         */
        private ConversationInfo info;
        
        /**
         * 应用类型
         */
        @JsonProperty("app_type")
        private Integer appType;
        
        /**
         * 对话主题
         */
        private String subject;
        
        /**
         * 远程IP
         */
        @JsonProperty("remote_ip")
        private String remoteIp;
        
        /**
         * IP地址信息
         */
        @JsonProperty("ip_address")
        private IPAddressInfo ipAddress;
        
        /**
         * 创建时间
         */
        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }
    
    /**
     * 对话信息
     */
    @Data
    public static class ConversationInfo {
        @JsonProperty("user_info")
        private UserInfo userInfo;
    }
    
    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        @JsonProperty("user_id")
        private String userId;
        
        private String name;
        
        private Integer from;
        
        @JsonProperty("real_name")
        private String realName;
        
        private String email;
        
        private String avatar;
    }
} 