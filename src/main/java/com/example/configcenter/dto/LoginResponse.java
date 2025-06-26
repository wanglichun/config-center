package com.example.configcenter.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    /**
     * JWT令牌
     */
    private String token;
    
    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String role;
        private String status;
    }
} 