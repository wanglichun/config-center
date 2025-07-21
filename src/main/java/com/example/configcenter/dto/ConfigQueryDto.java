package com.example.configcenter.dto;

import com.example.configcenter.common.PageBase;
import lombok.Data;

/**
 * 配置查询DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigQueryDto extends PageBase {
    /**
     * 配置组
     */
    private String groupName;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 状态
     */
    private String status;

} 