package com.chaitin.pandawiki.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chaitin.pandawiki.dto.CreateUserRequest;
import com.chaitin.pandawiki.dto.RegisterRequest;
import com.chaitin.pandawiki.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 创建用户
     */
    void createUser(CreateUserRequest request);

    /**
     * 修改密码
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(String userId);

    /**
     * 更新用户状态
     */
    void updateStatus(String userId, Boolean isActive);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 获取当前登录用户
     */
    User getCurrentUser();
}
