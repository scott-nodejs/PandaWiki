package com.chaitin.pandawiki.common;

import lombok.Data;

/**
 * 统一响应类
 */
@Data
public class Result<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    private Result() {
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
} 