package com.example.configcenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * 灰度发布DTO
 */
@Data
public class GrayReleaseDto {
    
    /**
     * 创建灰度发布计划请求
     */
    @Data
    public static class CreatePlanRequest {
        @NotBlank(message = "发布计划名称不能为空")
        private String planName;
        
        @NotBlank(message = "应用名称不能为空")
        private String appName;
        
        @NotBlank(message = "环境不能为空")
        private String environment;
        
        @NotBlank(message = "配置组不能为空")
        private String groupName;
        
        @NotNull(message = "配置键列表不能为空")
        private List<String> configKeys;
        
        @NotBlank(message = "灰度策略不能为空")
        private String grayStrategy;
        
        @NotNull(message = "灰度规则不能为空")
        private Map<String, Object> grayRules;
        
        @Min(value = 0, message = "发布百分比不能小于0")
        @Max(value = 100, message = "发布百分比不能大于100")
        private Integer rolloutPercentage = 0;
        
        private Boolean autoComplete = false;
        private Boolean autoRollback = false;
        private Map<String, Object> rollbackCondition;
        private String description;
    }
    
    /**
     * 灰度发布计划查询请求
     */
    @Data
    public static class PlanQueryRequest {
        private String appName;
        private String environment;
        private String status;
        private String creator;
        private Long startTime;
        private Long endTime;
        private Integer pageNum = 1;
        private Integer pageSize = 20;
    }
    
    /**
     * 灰度发布计划响应
     */
    @Data
    public static class PlanResponse {
        private Long id;
        private String planName;
        private String appName;
        private String environment;
        private String groupName;
        private List<String> configKeys;
        private String grayStrategy;
        private Map<String, Object> grayRules;
        private Integer rolloutPercentage;
        private String status;
        private String statusDesc;
        private Long startTime;
        private Long endTime;
        private Boolean autoComplete;
        private Boolean autoRollback;
        private Map<String, Object> rollbackCondition;
        private String description;
        private String creator;
        private String approver;
        private Long approvalTime;
        private Long createTime;
        private Long updateTime;
        
        // 统计信息
        private Integer totalConfigs;
        private Integer grayConfigs;
        private Integer publishedConfigs;
        private Integer rollbackConfigs;
    }
    
    /**
     * 灰度发布详情响应
     */
    @Data
    public static class DetailResponse {
        private Long id;
        private Long planId;
        private Long configId;
        private String configKey;
        private String oldValue;
        private String newValue;
        private String grayValue;
        private String status;
        private String statusDesc;
        private Long grayStartTime;
        private Long publishTime;
        private Long rollbackTime;
    }
    
    /**
     * 灰度发布操作请求
     */
    @Data
    public static class OperationRequest {
        @NotNull(message = "计划ID不能为空")
        private Long planId;
        
        @NotBlank(message = "操作类型不能为空")
        private String operationType;
        
        private String operationDesc;
        private Map<String, Object> params;
    }
    
    /**
     * 灰度规则
     */
    @Data
    public static class GrayRules {
        // IP白名单规则
        private List<String> ipWhitelist;
        
        // 用户白名单规则
        private List<String> userWhitelist;
        
        // 百分比规则
        private Integer percentage;
        
        // 金丝雀规则
        private CanaryRules canary;
    }
    
    /**
     * 金丝雀规则
     */
    @Data
    public static class CanaryRules {
        private List<String> canaryInstances;
        private Integer maxInstances;
        private Integer stepPercentage;
        private Integer stepInterval; // 分钟
    }
    
    /**
     * 回滚条件
     */
    @Data
    public static class RollbackCondition {
        private Double errorRateThreshold; // 错误率阈值
        private Double responseTimeThreshold; // 响应时间阈值
        private Integer timeWindowMinutes; // 时间窗口(分钟)
        private Boolean autoRollback; // 是否自动回滚
    }
} 