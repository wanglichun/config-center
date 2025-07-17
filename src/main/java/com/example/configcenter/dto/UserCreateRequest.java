package com.example.configcenter.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户创建请求对象
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class UserCreateRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "用户名只能包含字母、数字、下划线和横线")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^[1][3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 部门
     */
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String department;
    
    /**
     * 角色（ADMIN/DEVELOPER/VIEWER）
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|DEVELOPER|VIEWER)$", message = "角色只能是ADMIN、DEVELOPER或VIEWER")
    private String role;
    
    /**
     * 状态（ACTIVE/INACTIVE/LOCKED）
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE|LOCKED)$", message = "状态只能是ACTIVE、INACTIVE或LOCKED")
    private String status = "ACTIVE";
    
    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;
    
    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
} 