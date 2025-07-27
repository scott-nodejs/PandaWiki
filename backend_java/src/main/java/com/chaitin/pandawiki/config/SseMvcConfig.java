package com.chaitin.pandawiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * SSE MVC配置
 * 通过禁用事件发布来解决SSE异步请求中的Shiro异常问题
 */
@Slf4j
@Configuration
public class SseMvcConfig {

    /**
     * 配置DispatcherServlet禁用事件发布
     * 这将防止在异步请求完成后发布事件时出现Shiro相关异常
     */
    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        
        // 禁用请求处理事件发布，这是导致Shiro异常的根本原因
        dispatcherServlet.setPublishEvents(false);
        
        log.info("已配置DispatcherServlet禁用事件发布，防止SSE异步请求中的Shiro异常");
        
        return dispatcherServlet;
    }
} 