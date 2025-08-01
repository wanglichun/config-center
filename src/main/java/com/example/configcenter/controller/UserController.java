package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.*;
import com.example.configcenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<UserDto>> getUsers(UserQueryRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIpAddress(httpRequest);
        log.info("查询用户列表 - 参数: {}, IP: {}", request, clientIp);
        
        try {
            PageResult<UserDto> result = userService.getUsers(request);
            log.info("查询用户列表成功 - 总数: {}, 当前页: {}, IP: {}", 
                    result.getTotal(), result.getPageNum(), clientIp);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败 - IP: {}, 错误: {}", clientIp, e.getMessage());
            return ApiResult.error("查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UserDto> getUserById(@PathVariable @NotNull Long id, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        log.info("查询用户详情 - 用户ID: {}, IP: {}", id, clientIp);
        
        try {
            UserDto user = userService.getUserById(id);
            return ApiResult.success(user);
        } catch (Exception e) {
            log.error("查询用户详情失败 - 用户ID: {}, IP: {}, 错误: {}", id, clientIp, e.getMessage());
            return ApiResult.error("查询用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UserDto> createUser(@Valid @RequestBody UserCreateRequest request, 
                                        HttpServletRequest httpRequest) {
        String clientIp = getClientIpAddress(httpRequest);
        String operator = getCurrentUsername();
        log.info("创建用户 - 用户名: {}, 操作者: {}, IP: {}", request.getUsername(), operator, clientIp);
        
        try {
            UserDto user = userService.createUser(request);
            log.info("创建用户成功 - 用户ID: {}, 用户名: {}, 操作者: {}, IP: {}", 
                    user.getId(), user.getUsername(), operator, clientIp);
            return ApiResult.success(user);
        } catch (Exception e) {
            log.error("创建用户失败 - 用户名: {}, 操作者: {}, IP: {}, 错误: {}", 
                    request.getUsername(), operator, clientIp, e.getMessage());
            return ApiResult.error("创建用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UserDto> updateUser(@Valid @RequestBody UserUpdateRequest request, 
                                        HttpServletRequest httpRequest) {
        String clientIp = getClientIpAddress(httpRequest);
        String operator = getCurrentUsername();
        log.info("更新用户信息 - 用户ID: {}, 操作者: {}, IP: {}", request.getId(), operator, clientIp);
        
        try {
            UserDto user = userService.updateUser(request);
            log.info("更新用户信息成功 - 用户ID: {}, 操作者: {}, IP: {}", 
                    user.getId(), operator, clientIp);
            return ApiResult.success(user);
        } catch (Exception e) {
            log.error("更新用户信息失败 - 用户ID: {}, 操作者: {}, IP: {}, 错误: {}", 
                    request.getId(), operator, clientIp, e.getMessage());
            return ApiResult.error("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> deleteUser(@PathVariable @NotNull Long id, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String operator = getCurrentUsername();
        log.info("删除用户 - 用户ID: {}, 操作者: {}, IP: {}", id, operator, clientIp);
        
        try {
            userService.deleteUser(id);
            log.info("删除用户成功 - 用户ID: {}, 操作者: {}, IP: {}", id, operator, clientIp);
            return ApiResult.success("删除用户成功");
        } catch (Exception e) {
            log.error("删除用户失败 - 用户ID: {}, 操作者: {}, IP: {}, 错误: {}", 
                    id, operator, clientIp, e.getMessage());
            return ApiResult.error("删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> deleteUsers(@RequestBody @NotEmpty List<Long> ids, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String operator = getCurrentUsername();
        log.info("批量删除用户 - 用户IDs: {}, 操作者: {}, IP: {}", ids, operator, clientIp);
        
        try {
            userService.deleteUsers(ids);
            log.info("批量删除用户成功 - 删除数量: {}, 操作者: {}, IP: {}", ids.size(), operator, clientIp);
            return ApiResult.success("批量删除用户成功");
        } catch (Exception e) {
            log.error("批量删除用户失败 - 用户IDs: {}, 操作者: {}, IP: {}, 错误: {}", 
                    ids, operator, clientIp, e.getMessage());
            return ApiResult.error("批量删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> toggleUserStatus(@PathVariable @NotNull Long id, 
                                             @RequestParam @NotNull String status,
                                             HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String operator = getCurrentUsername();
        log.info("切换用户状态 - 用户ID: {}, 新状态: {}, 操作者: {}, IP: {}", id, status, operator, clientIp);
        
        try {
            userService.toggleUserStatus(id, status);
            log.info("切换用户状态成功 - 用户ID: {}, 新状态: {}, 操作者: {}, IP: {}", 
                    id, status, operator, clientIp);
            return ApiResult.success("用户状态更新成功");
        } catch (Exception e) {
            log.error("切换用户状态失败 - 用户ID: {}, 新状态: {}, 操作者: {}, IP: {}, 错误: {}", 
                    id, status, operator, clientIp, e.getMessage());
            return ApiResult.error("更新用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<String> resetPassword(@PathVariable @NotNull Long id, 
                                         @RequestParam @NotNull String newPassword,
                                         HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String operator = getCurrentUsername();
        log.info("重置用户密码 - 用户ID: {}, 操作者: {}, IP: {}", id, operator, clientIp);
        
        try {
            userService.resetPassword(id, newPassword);
            log.info("重置用户密码成功 - 用户ID: {}, 操作者: {}, IP: {}", id, operator, clientIp);
            return ApiResult.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置用户密码失败 - 用户ID: {}, 操作者: {}, IP: {}, 错误: {}", 
                    id, operator, clientIp, e.getMessage());
            return ApiResult.error("重置密码失败：" + e.getMessage());
        }
    }


    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Boolean> checkUsername(@RequestParam @NotNull String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            return ApiResult.success(exists);
        } catch (Exception e) {
            log.error("检查用户名失败 - 用户名: {}, 错误: {}", username, e.getMessage());
            return ApiResult.error("检查用户名失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ApiResult<UserDto> getCurrentUserProfile() {
        try {
            String username = getCurrentUsername();
            UserDto user = userService.getUserByUsername(username);
            return ApiResult.success(user);
        } catch (Exception e) {
            log.error("获取当前用户信息失败 - 错误: {}", e.getMessage());
            return ApiResult.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    public ApiResult<UserDto> updateCurrentUserProfile(@Valid @RequestBody UserUpdateRequest request,
                                                       HttpServletRequest httpRequest) {
        String clientIp = getClientIpAddress(httpRequest);
        String username = getCurrentUsername();
        Long userId = getCurrentUserId();
        
        // 确保只能更新自己的信息
        request.setId(userId);
        
        log.info("更新个人信息 - 用户: {}, IP: {}", username, clientIp);
        
        try {
            UserDto user = userService.updateUser(request);
            log.info("更新个人信息成功 - 用户: {}, IP: {}", username, clientIp);
            return ApiResult.success(user);
        } catch (Exception e) {
            log.error("更新个人信息失败 - 用户: {}, IP: {}, 错误: {}", username, clientIp, e.getMessage());
            return ApiResult.error("更新个人信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多IP的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            // 这里需要根据实际的认证机制来获取用户ID
            // 可以从JWT token中解析，或者查询数据库
            String username = getCurrentUsername();
            UserDto user = userService.getUserByUsername(username);
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
} 