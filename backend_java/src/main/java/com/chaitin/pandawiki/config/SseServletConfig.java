package com.chaitin.pandawiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SSE Servlet配置
 * 通过WebMvcConfigurer和自定义处理来解决SSE异步问题
 */
@Slf4j
@Configuration
public class SseServletConfig implements WebMvcConfigurer {

    /**
     * 配置异步支持，优化SSE处理
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间（30分钟）
        configurer.setDefaultTimeout(30 * 60 * 1000L);
        
        // 注册自定义的DeferredResult拦截器
        configurer.registerDeferredResultInterceptors(new SseDeferredResultInterceptor());
    }

    /**
     * SSE专用的DeferredResult拦截器
     * 用于在异步处理过程中处理SSE相关的上下文问题
     */
    private static class SseDeferredResultInterceptor implements org.springframework.web.context.request.async.DeferredResultProcessingInterceptor {
        
        @Override
        public <T> void beforeConcurrentHandling(org.springframework.web.context.request.NativeWebRequest request,
                                                org.springframework.web.context.request.async.DeferredResult<T> deferredResult) throws Exception {
            HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
            if (httpRequest != null && isSseRequest(httpRequest)) {
                log.debug("开始SSE异步处理: {}", httpRequest.getRequestURI());
                
                // 设置SSE处理标记
                httpRequest.setAttribute("SSE_ASYNC_PROCESSING", true);
            }
        }

        @Override
        public <T> void preProcess(org.springframework.web.context.request.NativeWebRequest request,
                                 org.springframework.web.context.request.async.DeferredResult<T> deferredResult) throws Exception {
            // 预处理阶段
        }

        @Override
        public <T> void postProcess(org.springframework.web.context.request.NativeWebRequest request,
                                  org.springframework.web.context.request.async.DeferredResult<T> deferredResult,
                                  Object concurrentResult) throws Exception {
            HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
            if (httpRequest != null && isSseRequest(httpRequest)) {
                log.debug("SSE异步处理完成: {}", httpRequest.getRequestURI());
            }
        }

        @Override
        public <T> boolean handleTimeout(org.springframework.web.context.request.NativeWebRequest request,
                                       org.springframework.web.context.request.async.DeferredResult<T> deferredResult) throws Exception {
            HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
            if (httpRequest != null && isSseRequest(httpRequest)) {
                log.warn("SSE异步处理超时: {}", httpRequest.getRequestURI());
            }
            return true; // 继续默认的超时处理
        }

        @Override
        public <T> boolean handleError(org.springframework.web.context.request.NativeWebRequest request,
                                     org.springframework.web.context.request.async.DeferredResult<T> deferredResult,
                                     Throwable ex) throws Exception {
            HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
            if (httpRequest != null && isSseRequest(httpRequest)) {
                // 检查是否是Shiro相关错误
                if (isShiroRelated(ex)) {
                    log.warn("SSE异步处理中出现Shiro相关错误，已处理: {} - {}", 
                            httpRequest.getRequestURI(), ex.getMessage());
                    return true; // 表示错误已处理，不需要进一步处理
                }
                log.error("SSE异步处理错误: " + httpRequest.getRequestURI(), ex);
            }
            return true; // 继续默认的错误处理
        }

        @Override
        public <T> void afterCompletion(org.springframework.web.context.request.NativeWebRequest request,
                                      org.springframework.web.context.request.async.DeferredResult<T> deferredResult) throws Exception {
            HttpServletRequest httpRequest = request.getNativeRequest(HttpServletRequest.class);
            if (httpRequest != null && isSseRequest(httpRequest)) {
                log.debug("SSE异步处理彻底完成: {}", httpRequest.getRequestURI());
                
                // 清理SSE相关的上下文
                httpRequest.removeAttribute("SSE_ASYNC_PROCESSING");
            }
        }

        /**
         * 检查是否是SSE请求
         */
        private boolean isSseRequest(HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            if (requestURI == null) {
                return false;
            }

            return requestURI.contains("/chat/") ||
                   Boolean.TRUE.equals(request.getAttribute("SSE_BYPASS_MODE")) ||
                   Boolean.TRUE.equals(request.getAttribute("SSE_REQUEST"));
        }

        /**
         * 检查异常是否与Shiro相关
         */
        private boolean isShiroRelated(Throwable throwable) {
            if (throwable == null) {
                return false;
            }
            
            String message = throwable.getMessage();
            String className = throwable.getClass().getName();
            
            return (message != null && message.contains("SecurityManager")) ||
                   (message != null && message.contains("Shiro")) ||
                   className.contains("shiro") ||
                   className.contains("SecurityManager");
        }
    }
} 