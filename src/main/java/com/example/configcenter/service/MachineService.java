package com.example.configcenter.service;

import com.example.configcenter.entity.MachineInstance;

import java.util.List;
import java.util.Set;

/**
 * 机器配置订阅服务接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public interface MachineService {

    /**
     * 注册机器实例
     *
     * @param groupName 配置组
     * @param instanceId 实例ID
     * @param instanceIp 实例IP
     * @param configKeys 订阅的配置键列表
     * @return 是否成功
     */
    boolean registerMachine(String groupName, String instanceId, String instanceIp, List<String> configKeys);

    /**
     * 获取订阅指定配置的机器列表
     *
     * @param groupName 配置组
     * @param configKey 配置键
     * @return 机器实例列表
     */
    List<MachineInstance> getSubscribedMachines(String groupName, String configKey);

    /**
     * 通知机器配置变更
     *
     * @param groupName 配置组
     * @param configKey 配置键
     * @param newValue 新配置值
     * @return 通知成功的机器数量
     */
    int notifyConfigChange(String groupName, String configKey, String newValue, Long version, List<String> ipList);
} 