package com.example.configcenter.service;

import java.util.List;
import java.util.Map;

/**
 * 机器配置订阅服务接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public interface MachineConfigSubscriptionService {

    /**
     * 注册机器实例
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @param instanceId 实例ID
     * @param instanceIp 实例IP
     * @param configKeys 订阅的配置键列表
     * @return 是否成功
     */
    boolean registerMachine(String appName, String environment, String groupName, 
                           String instanceId, String instanceIp, List<String> configKeys);

    /**
     * 注销机器实例
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @param instanceId 实例ID
     * @return 是否成功
     */
    boolean unregisterMachine(String appName, String environment, String groupName, String instanceId);

    /**
     * 获取订阅指定配置的机器列表
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @param configKey 配置键
     * @return 机器实例列表
     */
    List<String> getSubscribedMachines(String appName, String environment, String groupName, String configKey);

    /**
     * 通知机器配置变更
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @param configKey 配置键
     * @param newValue 新配置值
     * @return 通知成功的机器数量
     */
    int notifyConfigChange(String appName, String environment, String groupName, String configKey, String newValue);

    /**
     * 获取机器实例的配置信息
     *
     * @param instanceId 实例ID
     * @return 配置信息
     */
    Map<String, String> getMachineConfigs(String instanceId);

    /**
     * 心跳检测
     *
     * @param instanceId 实例ID
     * @return 是否存活
     */
    boolean heartbeat(String instanceId);
} 