package com.chaitin.pandawiki.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId
    private String id;

    @JsonProperty("account")
    private String username;

    private String password;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("last_access")
    private LocalDateTime lastAccess;

    private Boolean isActive;

    private Boolean isAdmin;
}
