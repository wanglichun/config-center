package com.example.configcenter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 灰度发布详情实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GrayReleaseDetail extends BaseEntity {
    
    /**
     * 发布计划ID
     */
    private Long planId;
    
    /**
     * 配置项ID
     */
    private Long configId;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 旧值
     */
    private String oldValue;
    
    /**
     * 新值
     */
    private String newValue;
    
    /**
     * 灰度值
     */
    private String grayValue;
    
    /**
     * 状态(PENDING/GRAY/PUBLISHED/ROLLBACK)
     */
    private String status;
    
    /**
     * 灰度开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long grayStartTime;
    
    /**
     * 全量发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long publishTime;
    
    /**
     * 回滚时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Long rollbackTime;
} 