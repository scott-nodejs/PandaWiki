package com.chaitin.pandawiki.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {
    
    @Schema(description = "访问令牌")
    private String token;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "账号")
    private String account;
} 