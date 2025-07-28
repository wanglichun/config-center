package com.example.configcenter.service.impl;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.*;
import com.example.configcenter.entity.User;
import com.example.configcenter.exception.BusinessException;
import com.example.configcenter.mapper.UserMapper;
import com.example.configcenter.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<UserDto> getUsers(UserQueryRequest request) {
        try {
            // 计算偏移量
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            
            // 查询用户列表
            List<User> users = userMapper.findByPage(offset, request.getPageSize(), request.getKeyword());
            
            // 查询总数量
            int total = userMapper.count(request.getKeyword());
            
            // 转换为DTO
            List<UserDto> userDtos = users.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return new PageResult<>(userDtos, total, request.getPageNum(), request.getPageSize());
            
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            throw new BusinessException("查询用户列表失败：" + e.getMessage());
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return convertToDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return convertToDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        try {
            // 验证用户名唯一性
            if (userMapper.existsByUsername(request.getUsername())) {
                throw new BusinessException("用户名已存在");
            }
            
            // 验证邮箱唯一性
            if (userMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
            
            // 创建用户对象
            User user = new User();
            BeanUtils.copyProperties(request, user);
            
            // 设置密码加密
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            // 设置创建信息
            String currentUser = getCurrentUsername();
            user.setCreateBy(currentUser);
            user.setUpdateBy(currentUser);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            // 保存用户
            int result = userMapper.insert(user);
            if (result <= 0) {
                throw new BusinessException("创建用户失败");
            }
            
            log.info("用户创建成功 - 用户名: {}, 创建者: {}", user.getUsername(), currentUser);
            return convertToDto(user);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建用户失败", e);
            throw new BusinessException("创建用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserDto updateUser(UserUpdateRequest request) {
        try {
            // 查询原用户信息
            User existingUser = userMapper.findById(request.getId());
            if (existingUser == null) {
                throw new BusinessException("用户不存在");
            }
            
            // 检查邮箱唯一性（排除自己）
            User userWithEmail = userMapper.findByEmail(request.getEmail());
            if (userWithEmail != null && !userWithEmail.getId().equals(request.getId())) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            
            // 更新用户信息
            User user = new User();
            BeanUtils.copyProperties(request, user);
            user.setUpdateBy(getCurrentUsername());
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.update(user);
            if (result <= 0) {
                throw new BusinessException("更新用户失败");
            }
            
            log.info("用户更新成功 - 用户ID: {}, 更新者: {}", user.getId(), getCurrentUsername());
            
            // 返回更新后的用户信息
            return getUserById(request.getId());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            throw new BusinessException("更新用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查用户是否存在
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除自己
        String currentUser = getCurrentUsername();
        if (user.getUsername().equals(currentUser)) {
            throw new BusinessException("不能删除自己的账号");
        }
        
        // 不能删除管理员（可以根据业务需求调整）
        if ("ADMIN".equals(user.getRole()) && "admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除超级管理员账号");
        }
        
        try {
            int result = userMapper.deleteById(id);
            if (result <= 0) {
                throw new BusinessException("删除用户失败");
            }
            
            log.info("用户删除成功 - 用户ID: {}, 用户名: {}, 操作者: {}", id, user.getUsername(), currentUser);
            
        } catch (Exception e) {
            log.error("删除用户失败", e);
            throw new BusinessException("删除用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id, String status) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (!StringUtils.hasText(status) || 
            !("ACTIVE".equals(status) || "INACTIVE".equals(status) || "LOCKED".equals(status))) {
            throw new BusinessException("用户状态值无效");
        }
        
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能禁用自己
        String currentUser = getCurrentUsername();
        if (user.getUsername().equals(currentUser) && !"ACTIVE".equals(status)) {
            throw new BusinessException("不能禁用自己的账号");
        }
        
        try {
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setStatus(status);
            updateUser.setUpdateBy(currentUser);
            updateUser.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.update(updateUser);
            if (result <= 0) {
                throw new BusinessException("更新用户状态失败");
            }
            
            log.info("用户状态更新成功 - 用户ID: {}, 新状态: {}, 操作者: {}", id, status, currentUser);
            
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            throw new BusinessException("更新用户状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20个字符之间");
        }
        
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        try {
            String encodedPassword = passwordEncoder.encode(newPassword);
            int result = userMapper.updatePassword(id, encodedPassword);
            if (result <= 0) {
                throw new BusinessException("重置密码失败");
            }
            
            log.info("用户密码重置成功 - 用户ID: {}, 操作者: {}", id, getCurrentUsername());
            
        } catch (Exception e) {
            log.error("重置用户密码失败", e);
            throw new BusinessException("重置密码失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不匹配");
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        
        try {
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            int result = userMapper.updatePassword(userId, encodedPassword);
            if (result <= 0) {
                throw new BusinessException("修改密码失败");
            }
            
            log.info("用户密码修改成功 - 用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("修改用户密码失败", e);
            throw new BusinessException("修改密码失败：" + e.getMessage());
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userMapper.existsByEmail(email);
    }

    @Override
    public UserService.UserStatistics getUserStatistics() {
        try {
            Map<String, Long> statsMap = userMapper.getUserStatistics();
            
            UserService.UserStatistics statistics = new UserService.UserStatistics();
            statistics.setTotalUsers(statsMap.getOrDefault("totalUsers", 0L));
            statistics.setActiveUsers(statsMap.getOrDefault("activeUsers", 0L));
            statistics.setInactiveUsers(statsMap.getOrDefault("inactiveUsers", 0L));
            statistics.setLockedUsers(statsMap.getOrDefault("lockedUsers", 0L));
            statistics.setAdminUsers(statsMap.getOrDefault("adminUsers", 0L));
            statistics.setDeveloperUsers(statsMap.getOrDefault("developerUsers", 0L));
            statistics.setViewerUsers(statsMap.getOrDefault("viewerUsers", 0L));
            
            return statistics;
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            throw new BusinessException("获取用户统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 转换User实体为UserDto
     */
    private UserDto convertToDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
} 