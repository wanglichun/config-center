package com.example.configcenter.dto;

import lombok.Data;
import java.util.List;

/**
 * 配置订阅者DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ConfigSubscriberDto {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置描述
     */
    private String configDescription;
    
    /**
     * 订阅的容器实例列表
     */
    private List<String> subscribers;
    
    /**
     * 订阅容器数量
     */
    private Integer subscriberCount;
    
    /**
     * 配置状态
     */
    private String status;
    
    public ConfigSubscriberDto() {}
    
    public ConfigSubscriberDto(String configKey, String configDescription, List<String> subscribers) {
        this.configKey = configKey;
        this.configDescription = configDescription;
        this.subscribers = subscribers;
        this.subscriberCount = subscribers != null ? subscribers.size() : 0;
    }
    
    public ConfigSubscriberDto(String configKey, List<String> subscribers) {
        this(configKey, null, subscribers);
    }
} 