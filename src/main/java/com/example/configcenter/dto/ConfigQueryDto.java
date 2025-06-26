package com.example.configcenter.dto;

import lombok.Data;

/**
 * 配置查询DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigQueryDto {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * 环境
     */
    private String environment;
    
    /**
     * 配置组
     */
    private String groupName;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 关键词搜索
     */
    private String keyword;
} 