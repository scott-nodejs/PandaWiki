package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话
 */
@Data
@TableName(value = "conversation", autoResultMap = true)
public class Conversation {
    /**
     * 会话ID
     */
    private String id;
    
    /**
     * 知识库ID
     */
    private String kbId;
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * 应用类型
     */
    private Integer appType;
    
    /**
     * 会话主题/标题
     */
    private String subject;
    
    /**
     * 远程IP
     */
    private String remoteIp;
    
    /**
     * 用户信息JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ConversationInfo info;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
    
    /**
     * 会话信息
     */
    @Data
    public static class ConversationInfo {
        private UserInfo userInfo;
    }
    
    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        private String userId;
        private String name;
        private Integer from; // 0: 私聊, 1: 群聊
        private String realName;
        private String email;
        private String avatar;
    }
} 