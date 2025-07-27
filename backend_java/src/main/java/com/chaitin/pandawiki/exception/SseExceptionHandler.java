package com.chaitin.pandawiki.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * SSE异常处理器
 * 专门处理SSE接口中的Shiro相关异常，避免影响正常功能
 */
@Slf4j
@ControllerAdvice
@Order(-1000) // 高优先级，优先处理
public class SseExceptionHandler {

    /**
     * 处理Shiro SecurityManager不可用异常
     * 通常发生在SSE异步处理完成后的事件发布阶段
     */
    @ExceptionHandler(UnavailableSecurityManagerException.class)
    public ResponseEntity<Void> handleShiroSecurityManagerException(
            UnavailableSecurityManagerException ex, HttpServletRequest request) {
        
        String requestURI = request.getRequestURI();
        
        // 如果是SSE相关接口，静默处理异常
        if (isSseRequest(requestURI)) {
            log.debug("SSE接口Shiro异常已处理: {} - {}", requestURI, ex.getMessage());
            return ResponseEntity.ok().build();
        }
        
        // 非SSE接口，记录错误并返回服务器错误
        log.error("非SSE接口Shiro SecurityManager异常: {}", requestURI, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 处理运行时异常中的Shiro相关问题
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Void> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        
        String requestURI = request.getRequestURI();
        
        // 检查是否为SSE接口且异常原因是Shiro相关
        if (isSseRequest(requestURI) && isShiroRelated(ex)) {
            log.debug("SSE接口运行时异常中的Shiro问题已处理: {} - {}", requestURI, ex.getMessage());
            return ResponseEntity.ok().build();
        }
        
        // 其他情况，让其他异常处理器处理
        throw ex;
    }

    /**
     * 判断是否为SSE请求
     */
    private boolean isSseRequest(String requestURI) {
        if (requestURI == null) {
            return false;
        }
        
        return requestURI.contains("/share/v1/chat/message") ||
               requestURI.contains("/share/v1/chat/widget") ||
               requestURI.contains("/client/v1/chat") ||
               requestURI.endsWith("/chat") ||
               requestURI.contains("/sse");
    }

    /**
     * 判断异常是否与Shiro相关
     */
    private boolean isShiroRelated(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        
        // 检查异常消息
        String message = throwable.getMessage();
        if (message != null && (
                message.contains("SecurityManager") || 
                message.contains("shiro") ||
                message.contains("Subject") ||
                message.contains("Authentication"))) {
            return true;
        }
        
        // 检查异常类型
        String className = throwable.getClass().getName();
        if (className.contains("shiro") || className.contains("security")) {
            return true;
        }
        
        // 递归检查异常原因
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isShiroRelated(cause);
        }
        
        return false;
    }
} 