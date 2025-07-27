package com.chaitin.pandawiki.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 页面访问统计实体
 * 
 * @author chaitin
 */
@Data
public class StatPage {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库ID
     */
    private String kbId;
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 场景类型
     * 1=欢迎页面, 2=节点页面, 3=聊天页面, 4=认证页面
     */
    private Integer scene;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 用户代理
     */
    private String ua;
    
    /**
     * 浏览器名称
     */
    private String browserName;
    
    /**
     * 浏览器操作系统
     */
    private String browserOs;
    
    /**
     * 来源页面
     */
    private String referer;
    
    /**
     * 来源站点
     */
    private String refererHost;
    
    /**
     * 国家/地区
     */
    private String country;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 