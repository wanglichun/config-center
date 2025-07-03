package com.example.configcenter.service.impl;

import com.example.configcenter.service.MachineGroupService;
import com.example.configcenter.service.ZooKeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 机器分组服务实现
 */
@Service
public class MachineGroupServiceImpl implements MachineGroupService {
    
    private static final Logger log = LoggerFactory.getLogger(MachineGroupServiceImpl.class);
    
    @Autowired
    private ZooKeeperService zooKeeperService;
    
    // 机器实例缓存
    private final Map<String, List<MachineInstance>> machineCache = new ConcurrentHashMap<>();
    
    // 分层配置
    private static final double LAYER_1_PERCENTAGE = 0.05;  // 5%
    private static final double LAYER_2_PERCENTAGE = 0.25;  // 25%
    private static final double LAYER_3_PERCENTAGE = 0.70;  // 70%
    
    // 最小机器数量保证
    private static final int MIN_LAYER_1_COUNT = 1;
    private static final int MIN_LAYER_2_COUNT = 2;
    
    @Override
    public void registerMachine(String appName, String environment, String groupName, String instanceId, Map<String, Object> instanceInfo) {
        try {
            log.info("注册机器: appName={}, environment={}, groupName={}, instanceId={}", appName, environment, groupName, instanceId);
            
            String groupKey = buildGroupKey(appName, environment, groupName);
            
            // 创建机器实例
            MachineInstance machine = new MachineInstance(instanceId, (String) instanceInfo.get("ip"));
            machine.setHostname((String) instanceInfo.get("hostname"));
            machine.setGroupName(groupName);
            machine.setRegion((String) instanceInfo.get("region"));
            machine.setZone((String) instanceInfo.get("zone"));
            machine.setStabilityScore(calculateStabilityScore(instanceInfo));
            machine.setLastHeartbeat(System.currentTimeMillis());
            machine.setMetadata(instanceInfo);
            
            // 添加到缓存
            machineCache.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(machine);
            
            // 注册到 ZooKeeper
            String zkPath = buildMachinePath(appName, environment, groupName, instanceId);
            zooKeeperService.publishConfig(zkPath, instanceId);
            
            log.info("机器注册成功: {}", instanceId);
            
        } catch (Exception e) {
            log.error("注册机器失败: {}", instanceId, e);
            throw new RuntimeException("注册机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public void unregisterMachine(String appName, String environment, String groupName, String instanceId) {
        try {
            log.info("注销机器: appName={}, environment={}, groupName={}, instanceId={}", appName, environment, groupName, instanceId);
            
            String groupKey = buildGroupKey(appName, environment, groupName);
            
            // 从缓存中移除
            List<MachineInstance> machines = machineCache.get(groupKey);
            if (machines != null) {
                machines.removeIf(m -> m.getInstanceId().equals(instanceId));
            }
            
            // 从 ZooKeeper 移除
            String zkPath = buildMachinePath(appName, environment, groupName, instanceId);
            zooKeeperService.deleteConfig(zkPath);
            
            log.info("机器注销成功: {}", instanceId);
            
        } catch (Exception e) {
            log.error("注销机器失败: {}", instanceId, e);
            throw new RuntimeException("注销机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getMachinesByGroup(String appName, String environment, String groupName) {
        try {
            String groupKey = buildGroupKey(appName, environment, groupName);
            List<MachineInstance> machines = machineCache.get(groupKey);
            
            if (CollectionUtils.isEmpty(machines)) {
                return new ArrayList<>();
            }
            
            return machines.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取分组机器失败: appName={}, environment={}, groupName={}", appName, environment, groupName, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> getGroups(String appName, String environment) {
        try {
            String prefix = buildGroupKey(appName, environment, "");
            return machineCache.keySet().stream()
                    .filter(key -> key.startsWith(prefix))
                    .map(key -> key.substring(prefix.length()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取分组列表失败: appName={}, environment={}", appName, environment, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, List<Map<String, Object>>> getAllMachinesByGroups(String appName, String environment) {
        try {
            String prefix = buildGroupKey(appName, environment, "");
            return machineCache.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().substring(prefix.length()),
                            entry -> entry.getValue().stream()
                                    .map(this::convertToMap)
                                    .collect(Collectors.toList())
                    ));
                    
        } catch (Exception e) {
            log.error("获取所有分组机器失败: appName={}, environment={}", appName, environment, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public void createGroup(String appName, String environment, String groupName, String description) {
        try {
            log.info("创建分组: appName={}, environment={}, groupName={}", appName, environment, groupName);
            
            String groupKey = buildGroupKey(appName, environment, groupName);
            machineCache.put(groupKey, new ArrayList<>());
            
            // 在 ZooKeeper 中创建分组路径
            String zkPath = buildGroupPath(appName, environment, groupName);
            zooKeeperService.createPersistentNode(zkPath, description);
            
            log.info("分组创建成功: {}", groupName);
            
        } catch (Exception e) {
            log.error("创建分组失败: {}", groupName, e);
            throw new RuntimeException("创建分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteGroup(String appName, String environment, String groupName) {
        try {
            log.info("删除分组: appName={}, environment={}, groupName={}", appName, environment, groupName);
            
            String groupKey = buildGroupKey(appName, environment, groupName);
            machineCache.remove(groupKey);
            
            // 从 ZooKeeper 删除分组路径
            String zkPath = buildGroupPath(appName, environment, groupName);
            zooKeeperService.deleteConfig(zkPath);
            
            log.info("分组删除成功: {}", groupName);
            
        } catch (Exception e) {
            log.error("删除分组失败: {}", groupName, e);
            throw new RuntimeException("删除分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public void watchGroup(String appName, String environment, String groupName, GroupChangeListener listener) {
        try {
            log.info("监听分组变化: appName={}, environment={}, groupName={}", appName, environment, groupName);
            
            String zkPath = buildGroupPath(appName, environment, groupName);
            zooKeeperService.watchNode(zkPath, (path, data, eventType) -> {
                // 处理分组变化
                refreshGroupMachines(appName, environment, groupName, listener);
            });
            
        } catch (Exception e) {
            log.error("监听分组变化失败: {}", groupName, e);
            throw new RuntimeException("监听分组变化失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<MachineInstance> getSubscribedMachines(String appName, String environment, List<String> configKeys) {
        try {
            log.info("获取订阅配置的机器列表: appName={}, environment={}, configKeys={}", appName, environment, configKeys);
            
            Set<MachineInstance> allMachines = new HashSet<>();
            
            // 遍历所有配置键，获取订阅的机器
            for (String configKey : configKeys) {
                List<MachineInstance> machines = getMachinesByConfig(appName, environment, configKey);
                allMachines.addAll(machines);
            }
            
            // 过滤掉不健康的机器
            List<MachineInstance> healthyMachines = allMachines.stream()
                    .filter(machine -> isHealthy(machine.getInstanceId()))
                    .collect(Collectors.toList());
            
            log.info("获取到订阅机器数量: 总数={}, 健康数={}", allMachines.size(), healthyMachines.size());
            
            return healthyMachines;
            
        } catch (Exception e) {
            log.error("获取订阅机器失败: appName={}, environment={}, configKeys={}", appName, environment, configKeys, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<MachineInstance> getMachinesByConfig(String appName, String environment, String configKey) {
        try {
            // 查询 ZooKeeper 中订阅了该配置的机器
            String configPath = buildConfigSubscriptionPath(appName, environment, configKey);
            List<String> subscribers = zooKeeperService.getChildren(configPath);
            
            if (CollectionUtils.isEmpty(subscribers)) {
                return new ArrayList<>();
            }
            
            // 从缓存中获取机器实例
            List<MachineInstance> machines = new ArrayList<>();
            for (String instanceId : subscribers) {
                MachineInstance machine = findMachineById(appName, environment, instanceId);
                if (machine != null) {
                    machines.add(machine);
                }
            }
            
            return machines;
            
        } catch (Exception e) {
            log.error("根据配置键获取机器失败: appName={}, environment={}, configKey={}", appName, environment, configKey, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public LayeredMachineGroups splitMachinesIntoLayers(List<MachineInstance> machines) {
        try {
            log.info("开始分层机器，总数: {}", machines.size());
            
            if (CollectionUtils.isEmpty(machines)) {
                return new LayeredMachineGroups(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }
            
            int totalCount = machines.size();
            
            // 计算每层机器数量
            int layer1Count = Math.max(MIN_LAYER_1_COUNT, (int) (totalCount * LAYER_1_PERCENTAGE));
            int layer2Count = Math.max(MIN_LAYER_2_COUNT, (int) (totalCount * LAYER_2_PERCENTAGE));
            int layer3Count = totalCount - layer1Count - layer2Count;
            
            // 确保不超过总数
            if (layer1Count + layer2Count > totalCount) {
                layer2Count = totalCount - layer1Count;
                layer3Count = 0;
            }
            
            // 按稳定性评分排序，稳定性高的机器优先作为第一层
            List<MachineInstance> sortedMachines = machines.stream()
                    .sorted((m1, m2) -> Double.compare(m2.getStabilityScore(), m1.getStabilityScore()))
                    .collect(Collectors.toList());
            
            // 分层
            List<MachineInstance> layer1 = sortedMachines.subList(0, layer1Count);
            List<MachineInstance> layer2 = sortedMachines.subList(layer1Count, layer1Count + layer2Count);
            List<MachineInstance> layer3 = layer3Count > 0 ? 
                    sortedMachines.subList(layer1Count + layer2Count, totalCount) : new ArrayList<>();
            
            LayeredMachineGroups groups = new LayeredMachineGroups(layer1, layer2, layer3);
            
            log.info("机器分层完成: 第一层={}, 第二层={}, 第三层={}", layer1.size(), layer2.size(), layer3.size());
            
            return groups;
            
        } catch (Exception e) {
            log.error("分层机器失败", e);
            throw new RuntimeException("分层机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public double getMachineStabilityScore(String instanceId) {
        try {
            // 从缓存中查找机器
            MachineInstance machine = findMachineById(null, null, instanceId);
            if (machine != null) {
                return machine.getStabilityScore();
            }
            
            // 如果找不到，返回默认分数
            return 0.5;
            
        } catch (Exception e) {
            log.error("获取机器稳定性评分失败: {}", instanceId, e);
            return 0.5;
        }
    }
    
    @Override
    public boolean isHealthy(String instanceId) {
        try {
            // 从缓存中查找机器
            MachineInstance machine = findMachineById(null, null, instanceId);
            if (machine == null) {
                return false;
            }
            
            // 检查心跳时间
            long currentTime = System.currentTimeMillis();
            long lastHeartbeat = machine.getLastHeartbeat();
            long heartbeatThreshold = 60000; // 1分钟
            
            return (currentTime - lastHeartbeat) < heartbeatThreshold;
            
        } catch (Exception e) {
            log.error("检查机器健康状态失败: {}", instanceId, e);
            return false;
        }
    }
    
    // 私有方法
    
    private String buildGroupKey(String appName, String environment, String groupName) {
        return String.format("%s:%s:%s", appName, environment, groupName);
    }
    
    private String buildGroupPath(String appName, String environment, String groupName) {
        return String.format("/config-center/%s/%s/groups/%s", appName, environment, groupName);
    }
    
    private String buildMachinePath(String appName, String environment, String groupName, String instanceId) {
        return String.format("/config-center/%s/%s/groups/%s/machines/%s", appName, environment, groupName, instanceId);
    }
    
    private String buildConfigSubscriptionPath(String appName, String environment, String configKey) {
        return String.format("/config-center/%s/%s/subscriptions/%s", appName, environment, configKey);
    }
    
    private Map<String, Object> convertToMap(MachineInstance machine) {
        Map<String, Object> map = new HashMap<>();
        map.put("instanceId", machine.getInstanceId());
        map.put("instanceIp", machine.getInstanceIp());
        map.put("hostname", machine.getHostname());
        map.put("groupName", machine.getGroupName());
        map.put("region", machine.getRegion());
        map.put("zone", machine.getZone());
        map.put("stabilityScore", machine.getStabilityScore());
        map.put("lastHeartbeat", machine.getLastHeartbeat());
        map.put("metadata", machine.getMetadata());
        return map;
    }
    
    private double calculateStabilityScore(Map<String, Object> instanceInfo) {
        // 基于多个因素计算稳定性评分
        double score = 0.5; // 基础分数
        
        // 根据运行时间
        Long uptime = (Long) instanceInfo.get("uptime");
        if (uptime != null) {
            score += Math.min(uptime / (24 * 3600 * 1000.0), 0.3); // 运行时间最多加0.3分
        }
        
        // 根据CPU使用率
        Double cpuUsage = (Double) instanceInfo.get("cpuUsage");
        if (cpuUsage != null) {
            score += (1.0 - cpuUsage) * 0.1; // CPU使用率低加分
        }
        
        // 根据内存使用率
        Double memoryUsage = (Double) instanceInfo.get("memoryUsage");
        if (memoryUsage != null) {
            score += (1.0 - memoryUsage) * 0.1; // 内存使用率低加分
        }
        
        return Math.min(score, 1.0); // 最大1.0分
    }
    
    private MachineInstance findMachineById(String appName, String environment, String instanceId) {
        for (List<MachineInstance> machines : machineCache.values()) {
            for (MachineInstance machine : machines) {
                if (machine.getInstanceId().equals(instanceId)) {
                    return machine;
                }
            }
        }
        return null;
    }
    
    private void refreshGroupMachines(String appName, String environment, String groupName, GroupChangeListener listener) {
        try {
            // 从 ZooKeeper 获取最新的机器列表
            String groupPath = buildGroupPath(appName, environment, groupName);
            List<String> currentMachines = zooKeeperService.getChildren(groupPath + "/machines");
            
            String groupKey = buildGroupKey(appName, environment, groupName);
            List<MachineInstance> cachedMachines = machineCache.get(groupKey);
            
            if (cachedMachines == null) {
                cachedMachines = new ArrayList<>();
                machineCache.put(groupKey, cachedMachines);
            }
            
            // 找出新增和删除的机器
            Set<String> currentSet = new HashSet<>(currentMachines);
            Set<String> cachedSet = cachedMachines.stream()
                    .map(MachineInstance::getInstanceId)
                    .collect(Collectors.toSet());
            
            // 新增的机器
            for (String instanceId : currentSet) {
                if (!cachedSet.contains(instanceId)) {
                    // 模拟机器信息
                    Map<String, Object> instanceInfo = new HashMap<>();
                    instanceInfo.put("instanceId", instanceId);
                    instanceInfo.put("ip", "192.168.1." + ThreadLocalRandom.current().nextInt(100, 200));
                    
                    listener.onMachineAdded(instanceId, instanceInfo);
                }
            }
            
            // 删除的机器
            for (String instanceId : cachedSet) {
                if (!currentSet.contains(instanceId)) {
                    listener.onMachineRemoved(instanceId);
                }
            }
            
        } catch (Exception e) {
            log.error("刷新分组机器失败", e);
        }
    }
}
