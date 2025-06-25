package com.example.configcenter.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 角色（ADMIN/DEVELOPER/VIEWER）
     */
    private String role;
    
    /**
     * 状态（ACTIVE/INACTIVE/LOCKED）
     */
    private String status;
    
    /**
     * 最后登录时间
     */
    private Long lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 头像URL
     */
    private String avatar;
} 