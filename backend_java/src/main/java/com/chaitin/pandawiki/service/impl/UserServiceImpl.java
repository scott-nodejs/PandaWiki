package com.chaitin.pandawiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chaitin.pandawiki.dto.CreateUserRequest;
import com.chaitin.pandawiki.dto.RegisterRequest;
import com.chaitin.pandawiki.entity.User;
import com.chaitin.pandawiki.mapper.UserMapper;
import com.chaitin.pandawiki.security.JwtUtils;
import com.chaitin.pandawiki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtils jwtUtils;

    @Override
    public String login(String username, String password) {
        User user = this.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 密码加密
        String encryptedPassword = new Sha256Hash(password).toHex();
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT令牌
        return jwtUtils.generateToken(user.getId(), user.getUsername(), user.getIsAdmin(), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 检查用户名是否存在
        if (this.getByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户实体
        User user = new User();
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(request.getUsername());
        user.setPassword(new Sha256Hash(request.getPassword()).toHex());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastAccess(LocalDateTime.now());
        user.setIsActive(true);
        user.setIsAdmin(false);

        // 保存用户
        this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // 检查用户名是否存在
        if (this.getByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户实体
        User user = new User();
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(request.getUsername());
        user.setPassword(new Sha256Hash(request.getPassword()).toHex());
        user.setCreatedAt(LocalDateTime.now());
        user.setLastAccess(LocalDateTime.now());
        user.setIsActive(true);
        user.setIsAdmin(false);

        // 保存用户
        this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        String encryptedOldPassword = new Sha256Hash(oldPassword).toHex();
        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        user.setPassword(new Sha256Hash(newPassword).toHex());
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否是admin用户
        User currentUser = this.getCurrentUser();
        if (user.getUsername().equals("admin") && userId.equals(currentUser.getId())) {
            throw new RuntimeException("请修改配置文件中的管理员密码并重启应用");
        }
        if (!currentUser.getUsername().equals("admin") && !userId.equals(currentUser.getId())) {
            throw new RuntimeException("只有管理员可以重置其他用户密码");
        }

        // 重置为默认密码：123456
        user.setPassword(new Sha256Hash("123456").toHex());
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String userId, Boolean isActive) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsActive(isActive);
        this.updateById(user);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public User getCurrentUser() {
        String userId = (String) SecurityUtils.getSubject().getPrincipal();
        return this.getById(userId);
    }
}
