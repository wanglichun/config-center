package com.example.configcenter.entity;

import com.example.configcenter.common.PageBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置历史实体
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigHistoryReq extends PageBase {
    Long configId;
} 