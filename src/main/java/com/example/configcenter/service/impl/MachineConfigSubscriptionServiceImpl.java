package com.example.configcenter.service.impl;

import com.example.configcenter.entity.MachineInstance;
import com.example.configcenter.exception.ConfigException;
import com.example.configcenter.service.MachineConfigSubscriptionService;
import com.example.configcenter.service.ZooKeeperService;
import com.example.configcenter.utils.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                                  String instanceName, String instanceIp, List<String> configKeys) {
        try {
            for (String configKey : configKeys) {
                // 创建机器实例信息
                String instancePath = buildInstancePath(appName, environment, groupName, configKey);
                String config = zooKeeperService.getConfig(instancePath);
                MachineInstance machineInstance = JsonUtil.jsonToObject(config, MachineInstance.class);
                boolean registered = false;
                if (machineInstance == null) {
                    machineInstance = new MachineInstance();
                    machineInstance.setInstanceName(instanceName);
                    machineInstance.setInstanceIp(Set.of(instanceIp));
                    registered = zooKeeperService.createNode(instancePath, JsonUtil.objectToStringIgnoreNull(machineInstance));

                } else {
                    machineInstance.getInstanceIp().add(instanceIp);
                    machineInstance.setInstanceName(instanceName);
                    registered = zooKeeperService.updateNode(instancePath, JsonUtil.objectToStringIgnoreNull(machineInstance));
                }

                if (!registered) {
                    throw new ConfigException(String.format("machine:[%s] register config key:[%s] failed", instanceIp, configKey));
                }
            }

            return true;
        } catch (Exception e) {
            log.error("注册机器实例失败: instanceName={}", instanceName, e);
            return false;
        }
    }



    @Override
    public Set<String> getSubscribedMachines(String appName, String environment, String groupName, String configKey)  {
        String configPath = buildInstancePath(appName, environment, groupName, "simple-container");
        String config = zooKeeperService.getConfig(configPath);
        MachineInstance machineInstance = JsonUtil.jsonToObject(config, MachineInstance.class);
        return machineInstance.getInstanceIp();
    }

    @Override
    public int notifyConfigChange(String appName, String environment, String groupName, String configKey, String newValue) {
        try {
            Set<String> subscribedMachines = getSubscribedMachines(appName, environment, groupName, configKey);
            int successCount = 0;
            for (String instanceIp : subscribedMachines) {
                if (notifyMachineConfigChange(instanceIp, appName, environment, groupName, configKey, newValue)) {
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
    private boolean notifyMachineConfigChange(String instanceIp, String appName, String environment,
                                            String groupName, String configKey, String newValue) {
        try {
            // 创建配置变更通知节点
            String notificationPath = buildNotificationPath(appName, environment, groupName, configKey, instanceIp);
            String notificationData = buildNotificationData(configKey, newValue, System.currentTimeMillis());
            
            // 创建临时节点，机器收到通知后会自动删除
            boolean created = zooKeeperService.createNode(notificationPath, notificationData);
            
            if (created) {
                log.debug("配置变更通知已发送: instanceId={}, configKey={}", instanceIp, configKey);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("通知机器配置变更失败: instanceId={}, configKey={}", instanceIp, configKey, e);
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
    private String buildNotificationPath(String appName, String environment, String groupName, String configKey, String instanceIp) {
        return String.format("/notifications/%s/%s/%s/%s/%s", appName, environment, groupName, configKey, instanceIp);
    }

    /**
     * 构建通知数据
     */
    private String buildNotificationData(String configKey, String newValue, long timestamp) {
        return String.format("{\"configKey\":\"%s\",\"newValue\":\"%s\",\"timestamp\":%d}", configKey, newValue, timestamp);
    }
} 