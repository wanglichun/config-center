package com.example.configcenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 配置查询DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigQueryDto {
    
    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    private String appName;
    
    /**
     * 环境
     */
    @NotBlank(message = "环境不能为空")
    private String environment;
    
    /**
     * 配置组
     */
    @NotBlank(message = "配置组不能为空")
    private String groupName;
    
    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
} 