package com.example.configcenter.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置历史实体
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigHistory extends BaseEntity {

    /**
     * 配置项ID
     */
    private Long configId;

    /**
     * 配置值（变更前）
     */
    private String oldValue;
    
    /**
     * 配置值（变更后）
     */
    private String newValue;
    
    /**
     * 版本号
     */
    private Long version;
    
    /**
     * 操作类型（CREATE/UPDATE/DELETE/PUBLISH/ROLLBACK）
     */
    private String operationType;

    /**
     * 操作者
     */
    private String operator;
    
    /**
     * 操作时间
     */
    private Long operateTime;
} 