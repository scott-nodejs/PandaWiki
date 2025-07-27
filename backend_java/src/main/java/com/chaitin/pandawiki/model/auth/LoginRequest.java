package com.chaitin.pandawiki.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "账号")
    private String username;

    @Schema(description = "密码")
    private String password;
}
