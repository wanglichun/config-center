package com.example.configcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.ConfigHistory;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.mapper.ConfigHistoryMapper;
import com.example.configcenter.service.ConfigService;
import com.example.configcenter.service.MachineConfigSubscriptionService;
import com.example.configcenter.service.ZooKeeperService;
import com.example.configcenter.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置服务实现类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigItemMapper configItemMapper;

    @Autowired
    private ConfigHistoryMapper configHistoryMapper;

    @Autowired
    private ZooKeeperService zooKeeperService;

    @Autowired
    private MachineConfigSubscriptionService machineConfigSubscriptionService;

    @Override
    @Cacheable(value = "config", key = "#appName + ':' + #environment + ':' + #groupName + ':' + #configKey")
    public ConfigItem getConfig(String appName, String environment, String groupName, String configKey) {
        try {
            ConfigItem configItem = configItemMapper.findByKey(appName, environment, groupName, configKey);
            if (configItem != null && Boolean.TRUE.equals(configItem.getEncrypted())) {
                // 解密配置值
                configItem.setConfigValue(decryptConfig(configItem.getConfigValue()));
            }
            return configItem;
        } catch (Exception e) {
            log.error("获取配置失败: appName={}, environment={}, groupName={}, configKey={}", 
                     appName, environment, groupName, configKey, e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "configs", key = "#appName + ':' + #environment")
    public List<ConfigItem> getConfigs(String appName, String environment) {
        try {
            List<ConfigItem> configs = configItemMapper.findByApp(appName, environment);
            return configs.stream().map(config -> {
                if (Boolean.TRUE.equals(config.getEncrypted())) {
                    config.setConfigValue(decryptConfig(config.getConfigValue()));
                }
                return config;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取应用配置失败: appName={}, environment={}", appName, environment, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "configMap", key = "#appName + ':' + #environment + ':' + #groupName")
    public Map<String, String> getConfigMap(String appName, String environment, String groupName) {
        try {
            List<ConfigItem> configs = configItemMapper.findByGroup(appName, environment, groupName);
            Map<String, String> configMap = new HashMap<>();
            
            for (ConfigItem config : configs) {
                String value = config.getConfigValue();
                if (Boolean.TRUE.equals(config.getEncrypted())) {
                    value = decryptConfig(value);
                }
                configMap.put(config.getConfigKey(), value);
            }
            
            return configMap;
        } catch (Exception e) {
            log.error("获取配置组失败: appName={}, environment={}, groupName={}", 
                     appName, environment, groupName, e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean createConfig(ConfigItem configItem) {
        try {
            // 设置基础信息
            configItem.setVersion(1L);
            configItem.setStatus("ACTIVE");
            configItem.setCreateTime(LocalDateTime.now());
            configItem.setUpdateTime(LocalDateTime.now());
            
            // 生成ZK路径
            String zkPath = buildZkPath(configItem.getEnvironment(),
                                      configItem.getGroupName(), configItem.getConfigKey());
            configItem.setZkPath(zkPath);
            
            // 加密处理
            if (Boolean.TRUE.equals(configItem.getEncrypted())) {
                configItem.setConfigValue(encryptConfig(configItem.getConfigValue()));
            }
            
            // 保存到数据库
            int result = configItemMapper.insert(configItem);
            
            if (result > 0) {
                // 记录历史
                recordHistory(configItem, "CREATE", null, configItem.getConfigValue(), 
                            "创建配置项", configItem.getCreateBy());
                
                log.info("配置项创建成功: {}", configItem.getConfigKey());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("创建配置项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean updateConfig(ConfigItem configItem) {
        try {
            ConfigItem oldConfig = configItemMapper.findById(configItem.getId());
            if (oldConfig == null) {
                log.warn("配置项不存在: {}", configItem.getId());
                return false;
            }
            
            String oldValue = oldConfig.getConfigValue();
            String newValue = configItem.getConfigValue();
            
            // 加密处理
            if (Boolean.TRUE.equals(configItem.getEncrypted())) {
                newValue = encryptConfig(newValue);
                configItem.setConfigValue(newValue);
            }
            
            // 版本号递增
            configItem.setVersion(oldConfig.getVersion() + 1);
            configItem.setUpdateTime(LocalDateTime.now());
            
            // 更新数据库
            int result = configItemMapper.update(configItem);
            
            if (result > 0) {
                // 记录历史
                recordHistory(configItem, "UPDATE", 
                            Boolean.TRUE.equals(oldConfig.getEncrypted()) ? decryptConfig(oldValue) : oldValue,
                            Boolean.TRUE.equals(configItem.getEncrypted()) ? decryptConfig(newValue) : configItem.getConfigValue(),
                            "更新配置项", configItem.getUpdateBy());
                
                log.info("配置项更新成功: {}", configItem.getConfigKey());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("更新配置项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean deleteConfig(Long id) {
        try {
            ConfigItem configItem = configItemMapper.findById(id);
            if (configItem == null) {
                log.warn("配置项不存在: {}", id);
                return false;
            }
            
            // 软删除
            configItem.setDelFlag(1);
            configItem.setUpdateTime(LocalDateTime.now());
            
            int result = configItemMapper.update(configItem);
            
            if (result > 0) {
                // 从ZK删除
                zooKeeperService.deleteConfig(configItem.getZkPath());
                
                // 记录历史
                recordHistory(configItem, "DELETE", configItem.getConfigValue(), null, 
                            "删除配置项", configItem.getUpdateBy());
                
                log.info("配置项删除成功: {}", configItem.getConfigKey());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("删除配置项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean publishConfig(Long id, String publisher) {
        try {
            ConfigItem configItem = configItemMapper.findById(id);
            if (configItem == null) {
                log.warn("配置项不存在: {}", id);
                return false;
            }
            
            String configValue = configItem.getConfigValue();
            if (Boolean.TRUE.equals(configItem.getEncrypted())) {
                configValue = decryptConfig(configValue);
            }
            
            // 发布到ZooKeeper
            boolean published = zooKeeperService.publishConfig(configItem.getZkPath(), configValue);
            
            if (published) {
                // 更新发布信息
                configItem.setLastPublishTime(System.currentTimeMillis());
                configItem.setPublisher(publisher);
                configItem.setStatus("PUBLISHED");
                configItem.setUpdateTime(LocalDateTime.now());
                configItemMapper.update(configItem);
                
                // 记录历史
                recordHistory(configItem, "PUBLISH", null, configValue, 
                            "发布配置", publisher);
                
                // 通知订阅的机器配置变更
                int notifiedCount = machineConfigSubscriptionService.notifyConfigChange(
                    configItem.getAppName(), configItem.getEnvironment(), 
                    configItem.getGroupName(), configItem.getConfigKey(), configValue);
                
                log.info("配置发布成功: {}, 通知机器数: {}", configItem.getConfigKey(), notifiedCount);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("发布配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean publishConfigs(String appName, String environment, String publisher) {
        try {
            List<ConfigItem> configs = configItemMapper.findByApp(appName, environment);
            int successCount = 0;
            
            for (ConfigItem config : configs) {
                if (publishConfig(config.getId(), publisher)) {
                    successCount++;
                }
            }
            
            log.info("批量发布配置完成: 总数={}, 成功={}", configs.size(), successCount);
            return successCount == configs.size();
        } catch (Exception e) {
            log.error("批量发布配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean rollbackConfig(Long id, Long targetVersion, String operator) {
        try {
            ConfigItem currentConfig = configItemMapper.findById(id);
            if (currentConfig == null) {
                log.warn("配置项不存在: {}", id);
                return false;
            }
            
            ConfigHistory targetHistory = configHistoryMapper.findByConfigAndVersion(id, targetVersion);
            if (targetHistory == null) {
                log.warn("目标版本不存在: configId={}, version={}", id, targetVersion);
                return false;
            }
            
            // 回滚配置值
            currentConfig.setConfigValue(targetHistory.getNewValue());
            currentConfig.setVersion(currentConfig.getVersion() + 1);
            currentConfig.setUpdateTime(LocalDateTime.now());
            currentConfig.setUpdateBy(operator);
            
            int result = configItemMapper.update(currentConfig);
            
            if (result > 0) {
                // 重新发布
                publishConfig(id, operator);
                
                // 记录历史
                recordHistory(currentConfig, "ROLLBACK", 
                            targetHistory.getOldValue(), targetHistory.getNewValue(), 
                            "回滚到版本: " + targetVersion, operator);
                
                log.info("配置回滚成功: configId={}, targetVersion={}", id, targetVersion);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("回滚配置失败", e);
            return false;
        }
    }

    @Override
    public List<ConfigHistory> getConfigHistory(Long configId) {
        try {
            return configHistoryMapper.findByConfigId(configId);
        } catch (Exception e) {
            log.error("获取配置历史失败: configId={}", configId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ConfigItem> getConfigPage(ConfigQueryDto queryDto) {
        try {
            // 将页码转换为偏移量 (pageNum从1开始，需要转换为从0开始的偏移量)
            int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
            queryDto.setPageNum(offset);
            return configItemMapper.search(queryDto);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public long getConfigCount(ConfigQueryDto queryDto) {
        try {
            return configItemMapper.countByQuery(queryDto);
        } catch (Exception e) {
            log.error("获取配置总数失败", e);
            return 0;
        }
    }


    @Override
    @Transactional
    public int importConfigs(List<ConfigItem> configItems, String operator) {
        int successCount = 0;
        
        for (ConfigItem configItem : configItems) {
            try {
                configItem.setCreateBy(operator);
                configItem.setUpdateBy(operator);
                
                if (createConfig(configItem)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("导入配置失败: {}", configItem.getConfigKey(), e);
            }
        }
        
        log.info("配置导入完成: 总数={}, 成功={}", configItems.size(), successCount);
        return successCount;
    }

    @Override
    public List<ConfigItem> exportConfigs(String appName, String environment) {
        return getConfigs(appName, environment);
    }

    @Override
    public boolean validateConfig(String configValue, String dataType) {
        if (!StringUtils.hasText(configValue)) {
            return false;
        }
        
        try {
            switch (dataType.toUpperCase()) {
                case "NUMBER":
                    Double.parseDouble(configValue);
                    break;
                case "BOOLEAN":
                    if (!"true".equalsIgnoreCase(configValue) && !"false".equalsIgnoreCase(configValue)) {
                        return false;
                    }
                    break;
                case "JSON":
                    JSON.parseObject(configValue);
                    break;
                case "STRING":
                default:
                    // 字符串类型不需要特殊验证
                    break;
            }
            return true;
        } catch (Exception e) {
            log.warn("配置格式验证失败: value={}, type={}", configValue, dataType, e);
            return false;
        }
    }

    @Override
    public String encryptConfig(String configValue) {
        try {
            return EncryptUtils.encrypt(configValue);
        } catch (Exception e) {
            log.error("加密配置失败", e);
            return configValue;
        }
    }

    @Override
    public String decryptConfig(String encryptedValue) {
        try {
            return EncryptUtils.decrypt(encryptedValue);
        } catch (Exception e) {
            log.error("解密配置失败", e);
            return encryptedValue;
        }
    }

    /**
     * 构建ZooKeeper路径
     */
    private String buildZkPath(String environment, String groupName, String configKey) {
        return String.format("/configs/%s/%s/%s", environment, groupName, configKey);
    }

    /**
     * 记录配置历史
     */
    private void recordHistory(ConfigItem configItem, String operationType, 
                             String oldValue, String newValue, String changeReason, String operator) {
        try {
            ConfigHistory history = new ConfigHistory();
            history.setConfigId(configItem.getId());
            history.setAppName(configItem.getAppName());
            history.setEnvironment(configItem.getEnvironment());
            history.setGroupName(configItem.getGroupName());
            history.setConfigKey(configItem.getConfigKey());
            history.setOldValue(oldValue);
            history.setNewValue(newValue);
            history.setVersion(configItem.getVersion());
            history.setOperationType(operationType);
            history.setChangeReason(changeReason);
            history.setOperator(operator);
            history.setOperateTime(System.currentTimeMillis());
            history.setCreateTime(LocalDateTime.now());
            history.setCreateBy(operator);
            
            configHistoryMapper.insert(history);
        } catch (Exception e) {
            log.error("记录配置历史失败", e);
        }
    }
} 