package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.dto.LoginRequest;
import com.example.configcenter.dto.LoginResponse;
import com.example.configcenter.entity.User;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 验证参数
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ApiResult.error("用户名不能为空");
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ApiResult.error("密码不能为空");
            }
            
            // 简单的用户验证（实际项目中应该查询数据库）
            if ("admin".equals(loginRequest.getUsername()) && "admin123".equals(loginRequest.getPassword())) {
                // 创建响应对象
                LoginResponse response = new LoginResponse();
                response.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMjQ3NjQwMCwiZXhwIjoxNjMyNTYyODAwfQ.mock-jwt-token");
                response.setTokenType("Bearer");
                
                // 创建用户信息
                LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
                userInfo.setId(1L);
                userInfo.setUsername("admin");
                userInfo.setRealName("管理员");
                userInfo.setEmail("admin@example.com");
                userInfo.setRole("ADMIN");
                userInfo.setStatus("ACTIVE");
                
                response.setUserInfo(userInfo);
                
                return ApiResult.success(response);
            } else {
                return ApiResult.error("用户名或密码错误");
            }
        } catch (Exception e) {
            return ApiResult.error("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResult<String> logout() {
        return ApiResult.success("退出登录成功");
    }
    
    /**
     * 获取当前用户信息
     */
//    @GetMapping("/userinfo")
//    public ApiResult<LoginResponse.UserInfo> getUserInfo() {
//        try {
//            // 在实际项目中，这里应该从JWT token中获取用户信息
//            // 现在返回模拟数据
//            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
//            userInfo.setId(1L);
//            userInfo.setUsername("admin");
//            userInfo.setRealName("管理员");
//            userInfo.setEmail("admin@example.com");
//            userInfo.setRole("ADMIN");
//            userInfo.setStatus("ACTIVE");
//
//            return ApiResult.success(userInfo);
//        } catch (Exception e) {
//            return ApiResult.error("获取用户信息失败：" + e.getMessage());
//        }
//    }
} 