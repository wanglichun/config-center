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

    @Override
    public boolean registerMachine(String groupName,
                                  String instanceName, String instanceIp, List<String> configKeys) {
        try {
            for (String configKey : configKeys) {
                // 创建机器实例信息
                String instancePath = buildInstancePath(groupName, configKey);
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
    public Set<String> getSubscribedMachines(String groupName, String configKey)  {
        String configPath = buildInstancePath(groupName, configKey);
        String config = zooKeeperService.getConfig(configPath);
        MachineInstance machineInstance = JsonUtil.jsonToObject(config, MachineInstance.class);
        return machineInstance != null ? machineInstance.getInstanceIp() : Set.of();
    }

    @Override
    public int notifyConfigChange(String groupName, String configKey, String newValue) {
        try {
            Set<String> subscribedMachines = getSubscribedMachines(groupName, configKey);
            int successCount = 0;
            for (String instanceIp : subscribedMachines) {
                if (notifyMachineConfigChange(instanceIp, groupName, configKey, newValue)) {
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
     * 通知单个机器配置变更
     */
    private boolean notifyMachineConfigChange(String instanceIp, String groupName, String configKey,
                                              String newValue) {
        try {
            // 创建配置变更通知节点
            String notificationPath = buildNotificationPath(groupName, configKey, instanceIp);
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
    private String buildInstancePath(String groupName, String instanceId) {
        return String.format("/instances/%s/%s", groupName, instanceId);
    }

    /**
     * 构建通知路径
     */
    private String buildNotificationPath(String groupName, String configKey, String instanceIp) {
        return String.format("/notifications/%s/%s/%s", groupName, configKey, instanceIp);
    }

    /**
     * 构建通知数据
     */
    private String buildNotificationData(String configKey, String newValue, long timestamp) {
        return String.format("{\"configKey\":\"%s\",\"newValue\":\"%s\",\"timestamp\":%d}", configKey, newValue, timestamp);
    }
} 