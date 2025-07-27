package com.chaitin.pandawiki.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest {
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
} 