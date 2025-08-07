package com.example.configcenter.dto;

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
     * 数据类型（STRING/NUMBER/BOOLEAN/JSON）
     */
    private String dataType;

    /**
     * 配置描述
     */
    private String description;
}
