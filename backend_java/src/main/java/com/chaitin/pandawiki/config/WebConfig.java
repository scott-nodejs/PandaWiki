package com.chaitin.pandawiki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源路径
 * 
 * @author chaitin
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: 添加认证拦截器
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置widget静态文件路径
        registry.addResourceHandler("/widget-bot.js", "/widget-bot.css")
                .addResourceLocations("file:../web/app/public/")
                .setCachePeriod(0); // 不缓存，便于开发调试
        
        // 配置其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 