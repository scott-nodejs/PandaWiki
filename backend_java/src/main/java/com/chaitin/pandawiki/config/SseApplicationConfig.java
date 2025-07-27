package com.chaitin.pandawiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SSE应用配置
 * 进一步优化SSE处理，防止Shiro相关问题
 */
@Slf4j
@Configuration
public class SseApplicationConfig {

    /**
     * 注册SSE请求属性清理过滤器
     * 在请求处理前后清理可能导致Shiro问题的属性
     */
    @Bean
    public FilterRegistrationBean<SseRequestCleanupFilter> sseRequestCleanupFilter() {
        FilterRegistrationBean<SseRequestCleanupFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SseRequestCleanupFilter());
        registrationBean.addUrlPatterns("/share/v1/chat/*", "/client/v1/chat/*", "/api/v1/sse/*");
        registrationBean.setOrder(1); // 在其他过滤器之后执行
        registrationBean.setName("sseRequestCleanupFilter");
        log.info("SSE请求清理过滤器已注册");
        return registrationBean;
    }

    /**
     * SSE请求清理过滤器
     * 在请求前后清理可能导致问题的属性
     */
    private static class SseRequestCleanupFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                FilterChain filterChain) throws ServletException, IOException {
            
            String requestURI = request.getRequestURI();
            
            // 只处理SSE相关请求
            if (!isSseRequest(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            log.debug("SSE请求清理开始: {}", requestURI);
            
            try {
                // 设置请求属性，标记为SSE请求
                request.setAttribute("__SSE_REQUEST__", true);
                request.setAttribute("__SKIP_SHIRO__", true);
                request.setAttribute("__DISABLE_EVENT_PUBLISHING__", true);
                
                // 继续过滤链
                filterChain.doFilter(request, response);
                
            } finally {
                // 清理请求属性，防止内存泄漏
                try {
                    request.removeAttribute("__SSE_REQUEST__");
                    request.removeAttribute("__SKIP_SHIRO__");
                    request.removeAttribute("__DISABLE_EVENT_PUBLISHING__");
                    
                    // 清理可能的Shiro相关线程本地变量
                    cleanupShiroThreadLocals();
                    
                } catch (Exception e) {
                    log.debug("清理SSE请求属性时发生异常: {}", e.getMessage());
                }
                
                log.debug("SSE请求清理完成: {}", requestURI);
            }
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
         * 清理Shiro的线程本地变量
         * 防止异步处理时的线程上下文问题
         */
        private void cleanupShiroThreadLocals() {
            try {
                // 尝试清理Shiro的ThreadContext
                Class<?> threadContextClass = Class.forName("org.apache.shiro.util.ThreadContext");
                java.lang.reflect.Method removeMethod = threadContextClass.getMethod("remove");
                removeMethod.invoke(null);
                log.debug("已清理Shiro ThreadContext");
            } catch (Exception e) {
                // 静默处理，因为这不是关键操作
                log.debug("清理Shiro ThreadContext时发生异常: {}", e.getMessage());
            }
        }
    }
} 