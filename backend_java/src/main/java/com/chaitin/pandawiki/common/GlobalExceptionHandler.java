package com.chaitin.pandawiki.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 检查是否是SSE接口
     */
    private boolean isSSERequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contentType = request.getHeader("Accept");
        
        // 检查是否是聊天相关的SSE接口
        boolean isChatRequest = uri.contains("/chat/") && 
                               (uri.contains("/message") || uri.contains("/widget"));
        
        // 检查是否期望text/event-stream响应
        boolean expectsSSE = contentType != null && 
                            contentType.contains("text/event-stream");
        
        return isChatRequest || expectsSSE;
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e, 
                                                      HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口参数验证失败，跳过异常处理: {}", e.getMessage());
            return null; // 不处理SSE接口的异常
        }
        
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数验证失败: {}", message);
        return ApiResponse.error(message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口参数绑定失败，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数绑定失败: {}", message);
        return ApiResponse.error(message);
    }

    /**
     * 处理约束验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e, 
                                                               HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口约束验证失败，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        String message = e.getMessage();
        log.warn("约束验证失败: {}", message);
        return ApiResponse.error(message);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public ApiResponse<Void> handleUnauthenticatedException(UnauthenticatedException e, 
                                                           HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口认证失败，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        log.warn("认证失败: {}", e.getMessage());
        return ApiResponse.error("未登录或登录已过期");
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public ApiResponse<Void> handleAuthorizationException(AuthorizationException e, 
                                                         HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口授权失败，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        log.warn("授权失败: {}", e.getMessage());
        return ApiResponse.error("权限不足");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口运行时异常，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        log.error("运行时异常: ", e);
        return ApiResponse.error(e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口系统异常，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        log.error("系统异常", e);
        return ApiResponse.error("系统异常，请稍后重试");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e, 
                                                           HttpServletRequest request) {
        if (isSSERequest(request)) {
            log.warn("SSE接口参数错误，跳过异常处理: {}", e.getMessage());
            return null;
        }
        
        log.warn("参数错误", e);
        return ApiResponse.error(e.getMessage());
    }
} 