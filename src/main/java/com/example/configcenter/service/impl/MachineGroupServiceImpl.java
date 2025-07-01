package com.example.configcenter.service.impl;

import com.example.configcenter.service.MachineGroupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于ZooKeeper的机器分组服务实现
 */
@Service
public class MachineGroupServiceImpl implements MachineGroupService {
    
    private static final Logger log = LoggerFactory.getLogger(MachineGroupServiceImpl.class);
    
    private static final String MACHINE_GROUP_ROOT = "/config-center/machine-groups";
    
    @Autowired
    private CuratorFramework curatorFramework;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 缓存监听器，避免重复创建
    private final Map<String, PathChildrenCache> cacheMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        try {
            // 确保根路径存在
            if (curatorFramework.checkExists().forPath(MACHINE_GROUP_ROOT) == null) {
                curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .forPath(MACHINE_GROUP_ROOT);
            }
            log.info("机器分组服务初始化完成");
        } catch (Exception e) {
            log.error("机器分组服务初始化失败", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        // 关闭所有缓存监听器
        cacheMap.values().forEach(cache -> {
            try {
                cache.close();
            } catch (Exception e) {
                log.error("关闭缓存监听器失败", e);
            }
        });
        cacheMap.clear();
    }
    
    @Override
    public void registerMachine(String appName, String environment, String groupName, 
                               String instanceId, Map<String, Object> instanceInfo) {
        try {
            String groupPath = buildGroupPath(appName, environment, groupName);
            String machinePath = groupPath + "/" + instanceId;
            
            // 确保分组路径存在
            if (curatorFramework.checkExists().forPath(groupPath) == null) {
                curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .forPath(groupPath);
            }
            
            // 注册机器信息（临时节点，机器下线时自动删除）
            String machineData = objectMapper.writeValueAsString(instanceInfo);
            if (curatorFramework.checkExists().forPath(machinePath) != null) {
                curatorFramework.setData().forPath(machinePath, machineData.getBytes());
            } else {
                curatorFramework.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(machinePath, machineData.getBytes());
            }
            
            log.info("机器注册成功: {}/{}/{} -> {}", appName, environment, groupName, instanceId);
            
        } catch (Exception e) {
            log.error("注册机器失败: {}/{}/{} -> {}", appName, environment, groupName, instanceId, e);
            throw new RuntimeException("注册机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public void unregisterMachine(String appName, String environment, String groupName, String instanceId) {
        try {
            String machinePath = buildGroupPath(appName, environment, groupName) + "/" + instanceId;
            
            if (curatorFramework.checkExists().forPath(machinePath) != null) {
                curatorFramework.delete().forPath(machinePath);
                log.info("机器注销成功: {}/{}/{} -> {}", appName, environment, groupName, instanceId);
            }
            
        } catch (Exception e) {
            log.error("注销机器失败: {}/{}/{} -> {}", appName, environment, groupName, instanceId, e);
            throw new RuntimeException("注销机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getMachinesByGroup(String appName, String environment, String groupName) {
        try {
            String groupPath = buildGroupPath(appName, environment, groupName);
            
            if (curatorFramework.checkExists().forPath(groupPath) == null) {
                return new ArrayList<>();
            }
            
            List<String> children = curatorFramework.getChildren().forPath(groupPath);
            List<Map<String, Object>> machines = new ArrayList<>();
            
            for (String instanceId : children) {
                String machinePath = groupPath + "/" + instanceId;
                byte[] data = curatorFramework.getData().forPath(machinePath);
                
                if (data != null && data.length > 0) {
                    Map<String, Object> machineInfo = objectMapper.readValue(data, 
                        new TypeReference<Map<String, Object>>() {});
                    machineInfo.put("instanceId", instanceId);
                    machines.add(machineInfo);
                }
            }
            
            return machines;
            
        } catch (Exception e) {
            log.error("获取分组机器失败: {}/{}/{}", appName, environment, groupName, e);
            throw new RuntimeException("获取分组机器失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> getGroups(String appName, String environment) {
        try {
            String appPath = buildAppPath(appName, environment);
            
            if (curatorFramework.checkExists().forPath(appPath) == null) {
                return new ArrayList<>();
            }
            
            return curatorFramework.getChildren().forPath(appPath);
            
        } catch (Exception e) {
            log.error("获取应用分组失败: {}/{}", appName, environment, e);
            throw new RuntimeException("获取应用分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, List<Map<String, Object>>> getAllMachinesByGroups(String appName, String environment) {
        try {
            Map<String, List<Map<String, Object>>> result = new HashMap<>();
            List<String> groups = getGroups(appName, environment);
            
            for (String groupName : groups) {
                List<Map<String, Object>> machines = getMachinesByGroup(appName, environment, groupName);
                result.put(groupName, machines);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取应用所有机器分组失败: {}/{}", appName, environment, e);
            throw new RuntimeException("获取应用所有机器分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public void createGroup(String appName, String environment, String groupName, String description) {
        try {
            String groupPath = buildGroupPath(appName, environment, groupName);
            
            if (curatorFramework.checkExists().forPath(groupPath) != null) {
                throw new RuntimeException("分组已存在: " + groupName);
            }
            
            // 创建分组节点，存储分组描述信息
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("name", groupName);
            groupInfo.put("description", description);
            groupInfo.put("createTime", System.currentTimeMillis());
            
            String groupData = objectMapper.writeValueAsString(groupInfo);
            curatorFramework.create()
                .creatingParentsIfNeeded()
                .forPath(groupPath, groupData.getBytes());
            
            log.info("创建机器分组成功: {}/{}/{}", appName, environment, groupName);
            
        } catch (Exception e) {
            log.error("创建机器分组失败: {}/{}/{}", appName, environment, groupName, e);
            throw new RuntimeException("创建机器分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteGroup(String appName, String environment, String groupName) {
        try {
            String groupPath = buildGroupPath(appName, environment, groupName);
            
            if (curatorFramework.checkExists().forPath(groupPath) != null) {
                // 递归删除分组及其下的所有机器节点
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(groupPath);
                log.info("删除机器分组成功: {}/{}/{}", appName, environment, groupName);
            }
            
        } catch (Exception e) {
            log.error("删除机器分组失败: {}/{}/{}", appName, environment, groupName, e);
            throw new RuntimeException("删除机器分组失败: " + e.getMessage());
        }
    }
    
    @Override
    public void watchGroup(String appName, String environment, String groupName, GroupChangeListener listener) {
        try {
            String groupPath = buildGroupPath(appName, environment, groupName);
            String cacheKey = groupPath;
            
            // 检查是否已经有监听器
            if (cacheMap.containsKey(cacheKey)) {
                log.warn("分组监听器已存在: {}", cacheKey);
                return;
            }
            
            // 确保分组路径存在
            if (curatorFramework.checkExists().forPath(groupPath) == null) {
                curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .forPath(groupPath);
            }
            
            // 创建路径缓存监听器
            PathChildrenCache cache = new PathChildrenCache(curatorFramework, groupPath, true);
            
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    try {
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                String addedInstanceId = getInstanceIdFromPath(event.getData().getPath());
                                Map<String, Object> addedInfo = objectMapper.readValue(event.getData().getData(),
                                    new TypeReference<Map<String, Object>>() {});
                                listener.onMachineJoined(addedInstanceId, addedInfo);
                                log.info("机器加入分组: {} -> {}", groupPath, addedInstanceId);
                                break;
                                
                            case CHILD_REMOVED:
                                String removedInstanceId = getInstanceIdFromPath(event.getData().getPath());
                                listener.onMachineLeft(removedInstanceId);
                                log.info("机器离开分组: {} -> {}", groupPath, removedInstanceId);
                                break;
                                
                            case CHILD_UPDATED:
                                String updatedInstanceId = getInstanceIdFromPath(event.getData().getPath());
                                Map<String, Object> updatedInfo = objectMapper.readValue(event.getData().getData(),
                                    new TypeReference<Map<String, Object>>() {});
                                // 可以添加机器信息更新的处理
                                log.info("机器信息更新: {} -> {}", groupPath, updatedInstanceId);
                                break;
                                
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        log.error("处理分组变化事件失败", e);
                    }
                }
            });
            
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            cacheMap.put(cacheKey, cache);
            
            log.info("开始监听分组变化: {}", groupPath);
            
        } catch (Exception e) {
            log.error("监听分组变化失败: {}/{}/{}", appName, environment, groupName, e);
            throw new RuntimeException("监听分组变化失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建应用路径
     */
    private String buildAppPath(String appName, String environment) {
        return MACHINE_GROUP_ROOT + "/" + appName + "/" + environment;
    }
    
    /**
     * 构建分组路径
     */
    private String buildGroupPath(String appName, String environment, String groupName) {
        return buildAppPath(appName, environment) + "/" + groupName;
    }
    
    /**
     * 从路径中提取实例ID
     */
    private String getInstanceIdFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
} 