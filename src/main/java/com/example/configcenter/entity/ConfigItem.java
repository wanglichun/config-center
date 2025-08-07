package com.example.configcenter.entity;

import com.example.configcenter.enums.ConfigStatusEnum;
import com.example.configcenter.enums.ConfigValueTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 配置项实体
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
     * 数据类型
     */
    private ConfigValueTypeEnum dataType;
    
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
    private ConfigStatusEnum status;
    
    /**
     * ZooKeeper路径
     */
    private String zkPath;
    /**
     * 发布者
     */
    private String publisher;
    /**
     * 配置
     */
    private String owner;

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