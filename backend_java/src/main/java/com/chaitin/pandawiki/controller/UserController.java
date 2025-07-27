package com.chaitin.pandawiki.controller;

import com.chaitin.pandawiki.common.ApiResponse;
import com.chaitin.pandawiki.dto.*;
import com.chaitin.pandawiki.entity.User;
import com.chaitin.pandawiki.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Validated @RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(Map.of("token", token));
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Void> register(@Validated @RequestBody RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        userService.register(request);
        return ApiResponse.success();
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("")
    public ApiResponse<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ApiResponse.success(user);
    }

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    @RequiresRoles("admin")
    public ApiResponse<List<User>> listUsers() {
        List<User> users = userService.list();
        return ApiResponse.success(users);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping("/create")
    @RequiresRoles("admin")
    public ApiResponse<Void> createUser(@Validated @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ApiResponse.success();
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/reset_password")
    @RequiresRoles("admin")
    public ApiResponse<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getUserId());
        return ApiResponse.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    @RequiresRoles("admin")
    public ApiResponse<Void> deleteUser(@Validated @RequestBody DeleteUserRequest request) {
        userService.removeById(request.getUserId());
        return ApiResponse.success();
    }
} 