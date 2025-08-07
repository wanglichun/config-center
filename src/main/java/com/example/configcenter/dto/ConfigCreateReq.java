package com.example.configcenter.dto;

import lombok.Data;

@Data
public class ConfigCreateReq {
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
}
