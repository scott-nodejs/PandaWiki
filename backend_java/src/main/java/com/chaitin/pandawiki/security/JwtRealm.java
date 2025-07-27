package com.chaitin.pandawiki.security;

import com.chaitin.pandawiki.entity.User;
import com.chaitin.pandawiki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * JWT认证和授权Realm
 */
@Component
@RequiredArgsConstructor
public class JwtRealm extends AuthorizingRealm {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userId = (String) principals.getPrimaryPrincipal();
        User user = userService.getById(userId);
        
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();
        
        // 设置角色
        if (user.getIsAdmin()) {
            roles.add("admin");
        }
        roles.add("user");
        
        info.setRoles(roles);
        return info;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authenticationToken;
        String token = (String) jwtToken.getCredentials();

        // 验证token
        if (!jwtUtils.validateToken(token)) {
            throw new AuthenticationException("无效的token");
        }

        // 获取用户信息
        String userId = jwtUtils.getUserIdFromToken(token);
        User user = userService.getById(userId);
        
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }
        
        if (!user.getIsActive()) {
            throw new DisabledAccountException("账号已被禁用");
        }

        return new SimpleAuthenticationInfo(userId, token, getName());
    }
} 