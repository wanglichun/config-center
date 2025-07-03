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
     * 获取订阅指定配置的机器列表
     */
    List<MachineInstance> getSubscribedMachines(String appName, String environment, List<String> configKeys);
    
    /**
     * 根据配置键获取订阅的机器列表
     */
    List<MachineInstance> getMachinesByConfig(String appName, String environment, String configKey);
    
    /**
     * 将机器列表分成三层
     */
    LayeredMachineGroups splitMachinesIntoLayers(List<MachineInstance> machines);
    
    /**
     * 获取机器的稳定性评分
     */
    double getMachineStabilityScore(String instanceId);
    
    /**
     * 检查机器健康状态
     */
    boolean isHealthy(String instanceId);
    
    /**
     * 分组变化监听器
     */
    interface GroupChangeListener {
        /**
         * 机器加入分组
         */
        void onMachineAdded(String instanceId, Map<String, Object> instanceInfo);
        
        /**
         * 机器离开分组
         */
        void onMachineRemoved(String instanceId);
        
        /**
         * 机器更新
         */
        void onMachineUpdated(String instanceId, Map<String, Object> instanceInfo);
    }
    
    /**
     * 机器实例
     */
    class MachineInstance {
        private String instanceId;
        private String instanceIp;
        private String hostname;
        private String groupName;
        private String region;
        private String zone;
        private double stabilityScore;
        private long lastHeartbeat;
        private Map<String, Object> metadata;
        
        // 构造方法
        public MachineInstance(String instanceId, String instanceIp) {
            this.instanceId = instanceId;
            this.instanceIp = instanceIp;
        }
        
        // Getters and Setters
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        
        public String getInstanceIp() { return instanceIp; }
        public void setInstanceIp(String instanceIp) { this.instanceIp = instanceIp; }
        
        public String getHostname() { return hostname; }
        public void setHostname(String hostname) { this.hostname = hostname; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getZone() { return zone; }
        public void setZone(String zone) { this.zone = zone; }
        
        public double getStabilityScore() { return stabilityScore; }
        public void setStabilityScore(double stabilityScore) { this.stabilityScore = stabilityScore; }
        
        public long getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    /**
     * 分层机器分组
     */
    class LayeredMachineGroups {
        private List<MachineInstance> layer1; // 金丝雀层 5%
        private List<MachineInstance> layer2; // 灰度层 25%
        private List<MachineInstance> layer3; // 全量层 70%
        
        public LayeredMachineGroups(List<MachineInstance> layer1, List<MachineInstance> layer2, List<MachineInstance> layer3) {
            this.layer1 = layer1;
            this.layer2 = layer2;
            this.layer3 = layer3;
        }
        
        // Getters and Setters
        public List<MachineInstance> getLayer1() { return layer1; }
        public void setLayer1(List<MachineInstance> layer1) { this.layer1 = layer1; }
        
        public List<MachineInstance> getLayer2() { return layer2; }
        public void setLayer2(List<MachineInstance> layer2) { this.layer2 = layer2; }
        
        public List<MachineInstance> getLayer3() { return layer3; }
        public void setLayer3(List<MachineInstance> layer3) { this.layer3 = layer3; }
        
        public int getTotalCount() {
            return layer1.size() + layer2.size() + layer3.size();
        }
    }
} 