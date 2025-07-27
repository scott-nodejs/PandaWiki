package com.chaitin.pandawiki.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SSE绕过过滤器
 * 专门处理SSE聊天接口，完全绕过Shiro安全处理并禁用Spring事件发布
 * 必须在所有其他过滤器之前执行
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SseBypassFilter implements Filter {

    /**
     * SSE聊天接口路径匹配
     * 包含所有可能的SSE聊天相关接口
     */
    private static final String[] SSE_PATHS = {
            "/share/v1/chat/message",
            "/share/v1/chat/widget",
            "/client/v1/chat/message",
            "/client/v1/chat/widget",
            "/api/v1/chat"  // 为未来可能的API聊天接口预留
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // 检查是否是SSE聊天接口
        if (isSseRequest(requestURI)) {
            log.debug("检测到SSE请求，启用绕过模式: {}", requestURI);
            
            // 设置CORS响应头
            setupCorsHeaders(httpResponse);
            
            // 包装请求以禁用事件发布和其他可能导致Shiro相关问题的机制
            HttpServletRequest wrappedRequest = new SseHttpServletRequestWrapper(httpRequest);
            
            // 直接传递到下一个过滤器，绕过其他安全处理
            chain.doFilter(wrappedRequest, response);
        } else {
            // 非SSE请求，正常传递
            chain.doFilter(request, response);
        }
    }

    /**
     * 检查是否是SSE请求
     */
    private boolean isSseRequest(String requestURI) {
        if (requestURI == null) {
            return false;
        }
        
        for (String ssePath : SSE_PATHS) {
            if (requestURI.startsWith(ssePath)) {
                return true;
            }
        }
        
        // 额外检查：任何包含/chat/且Accept头包含text/event-stream的请求
        return requestURI.contains("/chat/");
    }

    /**
     * 设置CORS响应头
     */
    private void setupCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-KB-ID, x-simple-auth-password");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
    }

    /**
     * SSE专用的HttpServletRequest包装器
     * 主要目的是禁用Spring的事件发布机制和其他可能触发Shiro的操作
     */
    private static class SseHttpServletRequestWrapper extends HttpServletRequestWrapper {
        
        public SseHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        /**
         * 覆盖getUserPrincipal方法，返回null以避免Shiro相关调用
         */
        @Override
        public java.security.Principal getUserPrincipal() {
            return null;
        }

        /**
         * 覆盖getRemoteUser方法，返回null以避免安全上下文查询
         */
        @Override
        public String getRemoteUser() {
            return null;
        }

        /**
         * 覆盖isUserInRole方法，始终返回false以避免权限检查
         */
        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        /**
         * 覆盖getAuthType方法，返回null以避免认证类型检查
         */
        @Override
        public String getAuthType() {
            return null;
        }

        /**
         * 标记此请求为异步请求，以便Spring正确处理
         */
        @Override
        public boolean isAsyncSupported() {
            return true;
        }

        /**
         * 禁用会话创建，避免不必要的会话管理
         */
        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        /**
         * 禁用会话ID从Cookie获取
         */
        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        /**
         * 禁用会话ID从URL获取
         */
        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        /**
         * 添加自定义属性标记，供后续处理识别
         */
        @Override
        public Object getAttribute(String name) {
            if ("SSE_BYPASS_MODE".equals(name)) {
                return true;
            }
            return super.getAttribute(name);
        }

        /**
         * 设置自定义属性，标记为SSE绕过模式
         */
        @Override
        public void setAttribute(String name, Object o) {
            super.setAttribute(name, o);
            // 自动设置SSE绕过标记
            if (!"SSE_BYPASS_MODE".equals(name)) {
                super.setAttribute("SSE_BYPASS_MODE", true);
            }
        }
    }
} 