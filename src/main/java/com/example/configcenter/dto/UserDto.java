package com.example.configcenter.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class UserDto {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
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
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建者
     */
    private String createBy;
    
    /**
     * 更新者
     */
    private String updateBy;
    
    /**
     * 备注
     */
    private String remark;
} 