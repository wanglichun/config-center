package com.example.configcenter.dto;

import com.example.configcenter.enums.ConfigValueTypeEnum;
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
     * 数据类型
     */
    private ConfigValueTypeEnum dataType;

    /**
     * 配置描述
     */
    private String description;

    private boolean encrypted;
}
