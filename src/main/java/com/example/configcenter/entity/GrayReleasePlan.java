package com.example.configcenter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 灰度发布计划实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GrayReleasePlan extends BaseEntity {
    
    /**
     * 发布计划名称
     */
    private String planName;
    
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
     * 配置键列表(JSON格式)
     */
    private String configKeys;
    
    /**
     * 灰度策略(IP_WHITELIST/USER_WHITELIST/PERCENTAGE/CANARY)
     */
    private String grayStrategy;
    
    /**
     * 灰度规则(JSON格式)
     */
    private String grayRules;
    
    /**
     * 发布百分比(0-100)
     */
    private Integer rolloutPercentage;
    
    /**
     * 状态(DRAFT/ACTIVE/PAUSED/COMPLETED/FAILED/ROLLBACK)
     */
    private String status;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long endTime;
    
    /**
     * 是否自动完成
     */
    private Boolean autoComplete;
    
    /**
     * 是否自动回滚
     */
    private Boolean autoRollback;
    
    /**
     * 回滚条件(JSON格式)
     */
    private String rollbackCondition;
    
    /**
     * 发布描述
     */
    private String description;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 审批者
     */
    private String approver;
    
    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long approvalTime;
} 