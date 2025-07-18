package com.example.configcenter.service.impl;

import com.example.configcenter.service.MachineConfigSubscriptionService;
import com.example.configcenter.service.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器配置订阅服务实现类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class MachineConfigSubscriptionServiceImpl implements MachineConfigSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(MachineConfigSubscriptionServiceImpl.class);

    @Autowired
    private ZooKeeperService zooKeeperService;

    // 机器实例信息缓存
    private final Map<String, MachineInstance> machineInstances = new ConcurrentHashMap<>();
    
    // 配置订阅关系缓存
    private final Map<String, Set<String>> configSubscribers = new ConcurrentHashMap<>();
    
    // 本地配置缓存 - 存储每台机器的本地配置
    private final Map<String, Map<String, String>> localConfigCache = new ConcurrentHashMap<>();

    @Override
    public boolean registerMachine(String appName, String environment, String groupName, 
                                  String instanceId, String instanceIp, List<String> configKeys) {
        try {
            // 创建机器实例信息
            MachineInstance instance = new MachineInstance();
            instance.setInstanceId(instanceId);
            instance.setInstanceIp(instanceIp);
            instance.setAppName(appName);
            instance.setEnvironment(environment);
            instance.setGroupName(groupName);
            instance.setConfigKeys(new HashSet<>(configKeys));
            instance.setRegisterTime(System.currentTimeMillis());
            instance.setLastHeartbeat(System.currentTimeMillis());

            // 注册到ZooKeeper
            String instancePath = buildInstancePath(appName, environment, groupName, instanceId);
            String instanceData = buildInstanceData(instance);
            boolean registered = zooKeeperService.createEphemeralNode(instancePath, instanceData);
            
            if (registered) {
                // 更新本地缓存
                machineInstances.put(instanceId, instance);
                
                // 更新订阅关系
                for (String configKey : configKeys) {
                    String configPath = buildConfigPath(appName, environment, groupName, configKey);
                    configSubscribers.computeIfAbsent(configPath, k -> ConcurrentHashMap.newKeySet()).add(instanceId);
                }
                
                log.info("机器实例注册成功: instanceId={}, appName={}, environment={}, groupName={}, configKeys={}", 
                        instanceId, appName, environment, groupName, configKeys);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("注册机器实例失败: instanceId={}", instanceId, e);
            return false;
        }
    }

    @Override
    public boolean unregisterMachine(String appName, String environment, String groupName, String instanceId) {
        try {
            // 从ZooKeeper删除
            String instancePath = buildInstancePath(appName, environment, groupName, instanceId);
            boolean removed = zooKeeperService.deleteConfig(instancePath);
            
            if (removed) {
                // 清理本地缓存
                MachineInstance instance = machineInstances.remove(instanceId);
                if (instance != null) {
                    // 清理订阅关系
                    for (String configKey : instance.getConfigKeys()) {
                        String configPath = buildConfigPath(appName, environment, groupName, configKey);
                        Set<String> subscribers = configSubscribers.get(configPath);
                        if (subscribers != null) {
                            subscribers.remove(instanceId);
                            if (subscribers.isEmpty()) {
                                configSubscribers.remove(configPath);
                            }
                        }
                    }
                }
                
                log.info("机器实例注销成功: instanceId={}", instanceId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("注销机器实例失败: instanceId={}", instanceId, e);
            return false;
        }
    }

    @Override
    public List<String> getSubscribedMachines(String appName, String environment, String groupName, String configKey) {
        String configPath = buildConfigPath(appName, environment, groupName, configKey);
        Set<String> subscribers = configSubscribers.get(configPath);
        return subscribers != null ? new ArrayList<>(subscribers) : new ArrayList<>();
    }

    @Override
    public int notifyConfigChange(String appName, String environment, String groupName, String configKey, String newValue) {
        try {
            List<String> subscribedMachines = getSubscribedMachines(appName, environment, groupName, configKey);
            int successCount = 0;
            
            for (String instanceId : subscribedMachines) {
                if (notifyMachineConfigChange(instanceId, appName, environment, groupName, configKey, newValue)) {
                    successCount++;
                }
            }
            
            log.info("配置变更通知完成: configKey={}, 订阅机器数={}, 通知成功数={}", 
                    configKey, subscribedMachines.size(), successCount);
            return successCount;
        } catch (Exception e) {
            log.error("通知配置变更失败: configKey={}", configKey, e);
            return 0;
        }
    }

    @Override
    public Map<String, String> getMachineConfigs(String instanceId) {
        MachineInstance instance = machineInstances.get(instanceId);
        if (instance == null) {
            return Collections.emptyMap();
        }
        
        // 从本地配置缓存获取
        Map<String, String> localConfigs = localConfigCache.get(instanceId);
//        if (localConfigs != null) {
//            return new HashMap<>(localConfigs);
//        }
        
        // 如果本地缓存没有，尝试从本地文件系统获取
        Map<String, String> configs = new HashMap<>();
        for (String configKey : instance.getConfigKeys()) {
            String configValue = getLocalConfigValue(instanceId, configKey);
            if (configValue != null) {
                configs.put(configKey, configValue);
            }
        }
        
        // 缓存到本地
        if (!configs.isEmpty()) {
            localConfigCache.put(instanceId, new HashMap<>(configs));
        }
        
        return configs;
    }
    
    /**
     * 从本地配置缓存获取配置值
     */
    private String getLocalConfigValue(String instanceId, String configKey) {
        try {
            // 1. 首先尝试从本地配置文件读取
            String localConfigFile = "/tmp/local-config/" + instanceId + "/" + configKey + ".properties";
            java.io.File file = new java.io.File(localConfigFile);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    props.load(fis);
                    return props.getProperty(configKey);
                }
            }
        } catch (Exception e) {
            log.error("获取本地配置失败: instanceId={}, configKey={}", instanceId, configKey, e);
        }
        return null;
    }
    
    /**
     * 获取默认配置值（用于演示）
     */
    private String getDefaultConfigValue(String configKey) {
        switch (configKey) {
            case "host":
                return "localhost";
            case "port":
                return "8080";
            case "database":
                return "testdb";
            case "redis.host":
                return "redis-server-01";
            case "redis.port":
                return "6379";
            default:
                return "default-value";
        }
    }

    @Override
    public boolean heartbeat(String instanceId) {
        MachineInstance instance = machineInstances.get(instanceId);
        if (instance != null) {
            instance.setLastHeartbeat(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    /**
     * 通知单个机器配置变更
     */
    private boolean notifyMachineConfigChange(String instanceId, String appName, String environment, 
                                            String groupName, String configKey, String newValue) {
        try {
            // 创建配置变更通知节点
            String notificationPath = buildNotificationPath(appName, environment, groupName, configKey, instanceId);
            String notificationData = buildNotificationData(configKey, newValue, System.currentTimeMillis());
            
            // 创建临时节点，机器收到通知后会自动删除
            boolean created = zooKeeperService.createEphemeralNode(notificationPath, notificationData);
            
            if (created) {
                log.debug("配置变更通知已发送: instanceId={}, configKey={}", instanceId, configKey);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("通知机器配置变更失败: instanceId={}, configKey={}", instanceId, configKey, e);
            return false;
        }
    }

    /**
     * 构建实例路径
     */
    private String buildInstancePath(String appName, String environment, String groupName, String instanceId) {
        return String.format("/instances/%s/%s/%s/%s", appName, environment, groupName, instanceId);
    }

    /**
     * 构建配置路径
     */
    private String buildConfigPath(String appName, String environment, String groupName, String configKey) {
        return String.format("/configs/%s/%s/%s/%s", appName, environment, groupName, configKey);
    }

    /**
     * 构建通知路径
     */
    private String buildNotificationPath(String appName, String environment, String groupName, String configKey, String instanceId) {
        return String.format("/config-center/notifications/%s/%s/%s/%s/%s", appName, environment, groupName, configKey, instanceId);
    }

    /**
     * 构建实例数据
     */
    private String buildInstanceData(MachineInstance instance) {
        return String.format("{\"instanceId\":\"%s\",\"instanceIp\":\"%s\",\"appName\":\"%s\",\"environment\":\"%s\",\"groupName\":\"%s\",\"registerTime\":%d,\"lastHeartbeat\":%d}",
                instance.getInstanceId(), instance.getInstanceIp(), instance.getAppName(), 
                instance.getEnvironment(), instance.getGroupName(), instance.getRegisterTime(), instance.getLastHeartbeat());
    }

    /**
     * 构建通知数据
     */
    private String buildNotificationData(String configKey, String newValue, long timestamp) {
        return String.format("{\"configKey\":\"%s\",\"newValue\":\"%s\",\"timestamp\":%d}", configKey, newValue, timestamp);
    }

    /**
     * 机器实例信息
     */
    private static class MachineInstance {
        private String instanceId;
        private String instanceIp;
        private String appName;
        private String environment;
        private String groupName;
        private Set<String> configKeys;
        private long registerTime;
        private long lastHeartbeat;

        // Getters and Setters
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        
        public String getInstanceIp() { return instanceIp; }
        public void setInstanceIp(String instanceIp) { this.instanceIp = instanceIp; }
        
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public Set<String> getConfigKeys() { return configKeys; }
        public void setConfigKeys(Set<String> configKeys) { this.configKeys = configKeys; }
        
        public long getRegisterTime() { return registerTime; }
        public void setRegisterTime(long registerTime) { this.registerTime = registerTime; }
        
        public long getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    }
} 