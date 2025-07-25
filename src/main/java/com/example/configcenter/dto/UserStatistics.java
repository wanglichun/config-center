package com.example.configcenter.dto;

import lombok.Data;

/**
 * 用户统计信息DTO
 */
@Data
public class UserStatistics {
    
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 活跃用户数
     */
    private Integer activeUsers;
    
    /**
     * 锁定用户数
     */
    private Integer lockedUsers;
    
    /**
     * 管理员数量
     */
    private Integer adminCount;
    
    /**
     * 开发者数量
     */
    private Integer developerCount;
    
    /**
     * 查看者数量
     */
    private Integer viewerCount;
    
    /**
     * 今日新增用户数
     */
    private Integer todayNewUsers;
    
    /**
     * 本月新增用户数
     */
    private Integer monthNewUsers;
} 