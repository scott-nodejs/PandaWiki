package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户访问记录实体
 */
@Data
@TableName("user_access")
public class UserAccess {
    
    @TableId
    private String id;
    
    private String userId;
    
    private String kbId;
    
    private String nodeId;
    
    private String remoteIp;
    
    private String userAgent;
    
    private String referer;
    
    private LocalDateTime accessTime;
    
    private Integer accessCount;
} 