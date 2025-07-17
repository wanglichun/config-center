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
     * 修改密码
     */
    void changePassword(Long userId, PasswordChangeRequest request);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 获取用户统计信息
     */
    UserStatistics getUserStatistics();
    
    /**
     * 用户统计信息内部类
     */
    class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long lockedUsers;
        private long adminUsers;
        private long developerUsers;
        private long viewerUsers;
        
        // getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        public long getLockedUsers() { return lockedUsers; }
        public void setLockedUsers(long lockedUsers) { this.lockedUsers = lockedUsers; }
        
        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }
        
        public long getDeveloperUsers() { return developerUsers; }
        public void setDeveloperUsers(long developerUsers) { this.developerUsers = developerUsers; }
        
        public long getViewerUsers() { return viewerUsers; }
        public void setViewerUsers(long viewerUsers) { this.viewerUsers = viewerUsers; }
    }
} 