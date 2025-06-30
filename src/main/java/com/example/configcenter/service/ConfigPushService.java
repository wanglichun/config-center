package com.example.configcenter.service;

import java.util.List;

/**
 * 配置推送服务接口
 */
public interface ConfigPushService {
    
    /**
     * 推送配置到ZooKeeper
     */
    void pushConfigToZooKeeper(String appName, String environment, 
                              String groupName, String configKey, String configValue);
    
    /**
     * 批量推送配置
     */
    void batchPushConfig(String appName, String environment, 
                        List<ConfigPushItem> configItems);
    
    /**
     * 推送灰度配置
     */
    void pushGrayConfig(Long planId);
    
    /**
     * 回滚灰度配置
     */
    void rollbackGrayConfig(Long planId);
    
    /**
     * 通知指定实例刷新配置
     */
    void notifyInstanceRefresh(String appName, String environment, 
                              List<String> instanceIds);
    
    /**
     * 配置推送项
     */
    class ConfigPushItem {
        private String configKey;
        private String configValue;
        private String oldValue;
        
        // getters and setters
        public String getConfigKey() { return configKey; }
        public void setConfigKey(String configKey) { this.configKey = configKey; }
        public String getConfigValue() { return configValue; }
        public void setConfigValue(String configValue) { this.configValue = configValue; }
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    }
} 