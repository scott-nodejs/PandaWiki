package com.chaitin.pandawiki.common;

import lombok.Data;

/**
 * API响应封装类
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage("");
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应，带消息
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage(message);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage(message);
        response.setSuccess(false);
        response.setData(null);
        return response;
    }
    
    /**
     * 错误响应（保持兼容性，忽略code参数）
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return error(message);
    }
} 