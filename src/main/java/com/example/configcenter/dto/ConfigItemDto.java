package com.example.configcenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 配置项DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigItemDto {
    
    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 100, message = "应用名称长度不能超过100字符")
    private String appName;
    
    /**
     * 环境
     */
    @NotBlank(message = "环境不能为空")
    @Size(max = 50, message = "环境名称长度不能超过50字符")
    private String environment;
    
    /**
     * 配置组
     */
    @NotBlank(message = "配置组不能为空")
    @Size(max = 100, message = "配置组名称长度不能超过100字符")
    private String groupName;
    
    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 200, message = "配置键长度不能超过200字符")
    private String configKey;
    
    /**
     * 配置值
     */
    @NotNull(message = "配置值不能为null")
    private String configValue;
    
    /**
     * 数据类型
     */
    @NotBlank(message = "数据类型不能为空")
    private String dataType = "STRING";
    
    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500字符")
    private String description;
    
    /**
     * 是否加密
     */
    private Boolean encrypted = false;
    
    /**
     * 标签
     */
    @Size(max = 200, message = "标签长度不能超过200字符")
    private String tags;
    
    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
} 