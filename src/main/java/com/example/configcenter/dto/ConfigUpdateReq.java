package com.example.configcenter.dto;

import com.example.configcenter.enums.ConfigValueTypeEnum;
import lombok.Data;

@Data
public class ConfigUpdateReq {
    /**
     * 配置项ID
     */
    private Long id;
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
}
