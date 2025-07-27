package com.chaitin.pandawiki.config;

import com.chaitin.pandawiki.security.JwtFilter;
import com.chaitin.pandawiki.security.JwtRealm;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 * 注意：SSE聊天接口通过SseBypassFilter完全绕过，这里的配置作为二重保障
 */
@Configuration
@RequiredArgsConstructor
public class ShiroConfig {

    private final JwtRealm jwtRealm;

    /**
     * 配置SecurityManager
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);

        // 关闭Shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        return securityManager;
    }

    /**
     * 配置Shiro过滤器
     * 注意：SSE接口已通过SseBypassFilter在更高优先级处理，这里配置作为备用保障
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager());

        // 添加自定义过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JwtFilter());
        factoryBean.setFilters(filterMap);

        // 设置过滤器链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        
        // === SSE聊天接口 - 完全排除，绝对优先级 ===
        // 这些路径完全绕过Shiro处理，避免任何安全上下文问题
        filterChainDefinitionMap.put("/share/v1/chat/**", "anon");
        filterChainDefinitionMap.put("/client/v1/chat/**", "anon");
        filterChainDefinitionMap.put("/api/v1/chat/**", "anon");
        
        // === 用户认证接口 ===
        filterChainDefinitionMap.put("/api/v1/user/login", "anon");
        filterChainDefinitionMap.put("/api/v1/user/register", "anon");
        
        // === Share分享接口 - 允许匿名访问 ===
        filterChainDefinitionMap.put("/share/**", "anon");
        
        // === Client客户端接口 - 允许匿名访问 ===
        filterChainDefinitionMap.put("/client/**", "anon");
        
        // === API文档和调试接口 ===
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        
        // === 静态资源 ===
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        
        // === 系统监控接口 ===
        filterChainDefinitionMap.put("/actuator/**", "anon");
        filterChainDefinitionMap.put("/health", "anon");
        filterChainDefinitionMap.put("/info", "anon");
        
        // === 其他所有接口需要认证 ===
        filterChainDefinitionMap.put("/**", "jwt");
        
        factoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        
        return factoryBean;
    }

    /**
     * 开启Shiro注解支持
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager());
        return advisor;
    }
}