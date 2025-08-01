package com.example.configcenter.service;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.*;
import com.example.configcenter.entity.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public interface UserService {
    
    /**
     * 分页查询用户列表
     */
    PageResult<UserDto> getUsers(UserQueryRequest request);
    
    /**
     * 根据ID查询用户
     */
    UserDto getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    UserDto getUserByUsername(String username);
    
    /**
     * 创建用户
     */
    UserDto createUser(UserCreateRequest request);
    
    /**
     * 更新用户信息
     */
    UserDto updateUser(UserUpdateRequest request);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 批量删除用户
     */
    void deleteUsers(List<Long> ids);
    
    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long id, String status);
    
    /**
     * 重置用户密码
     */
    void resetPassword(Long id, String newPassword);
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
} 