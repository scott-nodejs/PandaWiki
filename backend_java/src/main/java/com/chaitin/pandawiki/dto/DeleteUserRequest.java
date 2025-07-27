package com.chaitin.pandawiki.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 删除用户请求
 */
@Data
public class DeleteUserRequest {
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
} 