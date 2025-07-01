package com.example.configcenter.service;

import java.util.List;
import java.util.Map;

/**
 * 机器分组服务接口
 * 通过ZooKeeper实现机器的分组管理和订阅
 */
public interface MachineGroupService {
    
    /**
     * 注册机器到指定分组
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     * @param instanceId 实例ID
     * @param instanceInfo 实例信息（IP、端口等）
     */
    void registerMachine(String appName, String environment, String groupName, 
                        String instanceId, Map<String, Object> instanceInfo);
    
    /**
     * 从分组中移除机器
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     * @param instanceId 实例ID
     */
    void unregisterMachine(String appName, String environment, String groupName, String instanceId);
    
    /**
     * 获取指定分组的所有机器
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     * @return 机器实例列表
     */
    List<Map<String, Object>> getMachinesByGroup(String appName, String environment, String groupName);
    
    /**
     * 获取应用的所有分组
     * @param appName 应用名称
     * @param environment 环境
     * @return 分组列表
     */
    List<String> getGroups(String appName, String environment);
    
    /**
     * 获取应用的所有机器（按分组组织）
     * @param appName 应用名称
     * @param environment 环境
     * @return 分组和机器的映射
     */
    Map<String, List<Map<String, Object>>> getAllMachinesByGroups(String appName, String environment);
    
    /**
     * 创建机器分组
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     * @param description 分组描述
     */
    void createGroup(String appName, String environment, String groupName, String description);
    
    /**
     * 删除机器分组
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     */
    void deleteGroup(String appName, String environment, String groupName);
    
    /**
     * 监听分组变化
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 分组名称
     * @param listener 变化监听器
     */
    void watchGroup(String appName, String environment, String groupName, GroupChangeListener listener);
    
    /**
     * 分组变化监听器
     */
    interface GroupChangeListener {
        /**
         * 机器加入分组
         */
        void onMachineJoined(String instanceId, Map<String, Object> instanceInfo);
        
        /**
         * 机器离开分组
         */
        void onMachineLeft(String instanceId);
        
        /**
         * 分组被删除
         */
        void onGroupDeleted();
    }
} 