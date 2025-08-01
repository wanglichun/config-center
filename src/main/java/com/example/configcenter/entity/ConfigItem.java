package com.example.configcenter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配置项实体
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigItem {
    
    /**
     * 配置项ID
     */
    private Long id;

    /**
     * 配置组
     */
    private String groupName;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 数据类型（STRING/NUMBER/BOOLEAN/JSON）
     */
    private String dataType;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 版本号
     */
    private Long version;
    
    /**
     * 是否加密
     */
    private Boolean encrypted;
    
    /**
     * 标签（用于分类）
     */
    private String tags;
    
    /**
     * 状态（ACTIVE/INACTIVE）
     */
    private String status;
    
    /**
     * ZooKeeper路径
     */
    private String zkPath;
    
    /**
     * 最后发布时间
     */
    private Long lastPublishTime;
    
    /**
     * 发布者
     */
    private String publisher;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 逻辑删除标志（0存在 1删除）
     */
    private Integer delFlag;
} 