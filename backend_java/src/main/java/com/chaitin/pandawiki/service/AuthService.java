package com.chaitin.pandawiki.service;

import com.chaitin.pandawiki.model.auth.LoginRequest;
import com.chaitin.pandawiki.model.auth.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 用户ID
     */
    String validateToken(String token);
    
    /**
     * 生成令牌
     *
     * @param userId 用户ID
     * @return 访问令牌
     */
    String generateToken(String userId);
} 