package com.example.configcenter.service.impl;

import com.example.configcenter.service.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZooKeeper服务实现类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ZooKeeperServiceImpl implements ZooKeeperService {

    @Autowired
    private CuratorFramework curatorFramework;

    private final Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<>();
    private final Map<String, PathChildrenCache> pathChildrenCacheMap = new ConcurrentHashMap<>();

    @Override
    public boolean publishConfig(String path, String data) {
        try {
            if (exists(path)) {
                return updateNode(path, data);
            } else {
                return createPersistentNode(path, data);
            }
        } catch (Exception e) {
            log.error("发布配置失败: path={}, data={}", path, data, e);
            return false;
        }
    }

    @Override
    public String getConfig(String path) {
        try {
            if (!exists(path)) {
                log.warn("配置节点不存在: {}", path);
                return null;
            }
            
            byte[] data = curatorFramework.getData().forPath(path);
            return data != null ? new String(data) : null;
        } catch (Exception e) {
            log.error("获取配置失败: path={}", path, e);
            return null;
        }
    }

    @Override
    public boolean deleteConfig(String path) {
        try {
            if (!exists(path)) {
                log.warn("要删除的配置节点不存在: {}", path);
                return true;
            }
            
            curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            
            // 清理缓存
            NodeCache nodeCache = nodeCacheMap.remove(path);
            if (nodeCache != null) {
                nodeCache.close();
            }
            
            PathChildrenCache pathChildrenCache = pathChildrenCacheMap.remove(path);
            if (pathChildrenCache != null) {
                pathChildrenCache.close();
            }
            
            log.info("删除配置节点成功: {}", path);
            return true;
        } catch (Exception e) {
            log.error("删除配置失败: path={}", path, e);
            return false;
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            if (!exists(path)) {
                log.warn("父节点不存在: {}", path);
                return Collections.emptyList();
            }
            
            return curatorFramework.getChildren().forPath(path);
        } catch (Exception e) {
            log.error("获取子节点失败: path={}", path, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            log.error("检查节点存在性失败: path={}", path, e);
            return false;
        }
    }

    @Override
    public boolean createPersistentNode(String path, String data) {
        try {
            // 创建父节点
            createParentNodes(path);
            
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, data != null ? data.getBytes() : null);
            
            log.info("创建持久节点成功: path={}, data={}", path, data);
            return true;
        } catch (Exception e) {
            log.error("创建持久节点失败: path={}, data={}", path, data, e);
            return false;
        }
    }

    @Override
    public boolean createEphemeralNode(String path, String data) {
        try {
            // 创建父节点
            createParentNodes(path);
            
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path, data != null ? data.getBytes() : null);
            
            log.info("创建临时节点成功: path={}, data={}", path, data);
            return true;
        } catch (Exception e) {
            log.error("创建临时节点失败: path={}, data={}", path, data, e);
            return false;
        }
    }

    @Override
    public boolean updateNode(String path, String data) {
        try {
            if (!exists(path)) {
                log.warn("要更新的节点不存在: {}", path);
                return false;
            }
            
            curatorFramework.setData().forPath(path, data != null ? data.getBytes() : null);
            log.info("更新节点成功: path={}, data={}", path, data);
            return true;
        } catch (Exception e) {
            log.error("更新节点失败: path={}, data={}", path, data, e);
            return false;
        }
    }

    @Override
    public void watchNode(String path, ConfigChangeListener listener) {
        try {
            NodeCache nodeCache = nodeCacheMap.get(path);
            if (nodeCache == null) {
                nodeCache = new NodeCache(curatorFramework, path);
                nodeCacheMap.put(path, nodeCache);
                
                nodeCache.getListenable().addListener(() -> {
                    try {
                        byte[] data = nodeCache.getCurrentData() != null ? 
                                     nodeCache.getCurrentData().getData() : null;
                        String dataStr = data != null ? new String(data) : null;
                        listener.onConfigChanged(path, dataStr, "NODE_UPDATED");
                    } catch (Exception e) {
                        log.error("处理节点变化事件失败: path={}", path, e);
                    }
                });
                
                nodeCache.start(NodeCache.StartMode.BUILD_INITIAL_CACHE);
            }
            
            log.info("开始监听节点变化: {}", path);
        } catch (Exception e) {
            log.error("监听节点失败: path={}", path, e);
        }
    }

    @Override
    public void watchChildren(String path, ConfigChangeListener listener) {
        try {
            PathChildrenCache pathChildrenCache = pathChildrenCacheMap.get(path);
            if (pathChildrenCache == null) {
                pathChildrenCache = new PathChildrenCache(curatorFramework, path,  true);
                pathChildrenCacheMap.put(path, pathChildrenCache);
                
                pathChildrenCache.getListenable().addListener((client, event) -> {
                    try {
                        String eventPath = event.getData() != null ? event.getData().getPath() : path;
                        String data = event.getData() != null && event.getData().getData() != null ? 
                                     new String(event.getData().getData()) : null;
                        
                        String eventType;
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                eventType = "CHILD_ADDED";
                                break;
                            case CHILD_UPDATED:
                                eventType = "CHILD_UPDATED";
                                break;
                            case CHILD_REMOVED:
                                eventType = "CHILD_REMOVED";
                                break;
                            default:
                                eventType = "UNKNOWN";
                                break;
                        }
                        
                        listener.onConfigChanged(eventPath, data, eventType);
                    } catch (Exception e) {
                        log.error("处理子节点变化事件失败: path={}", path, e);
                    }
                });
                
                pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            }
            
            log.info("开始监听子节点变化: {}", path);
        } catch (Exception e) {
            log.error("监听子节点失败: path={}", path, e);
        }
    }

    @Override
    public Map<String, Object> getNodeStat(String path) {
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            if (stat == null) {
                return Collections.emptyMap();
            }
            
            Map<String, Object> statMap = new HashMap<>();
            statMap.put("czxid", stat.getCzxid());
            statMap.put("mzxid", stat.getMzxid());
            statMap.put("ctime", stat.getCtime());
            statMap.put("mtime", stat.getMtime());
            statMap.put("version", stat.getVersion());
            statMap.put("cversion", stat.getCversion());
            statMap.put("aversion", stat.getAversion());
            statMap.put("ephemeralOwner", stat.getEphemeralOwner());
            statMap.put("dataLength", stat.getDataLength());
            statMap.put("numChildren", stat.getNumChildren());
            statMap.put("pzxid", stat.getPzxid());
            
            return statMap;
        } catch (Exception e) {
            log.error("获取节点状态失败: path={}", path, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 创建父节点
     */
    private void createParentNodes(String path) throws Exception {
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        if (!parentPath.isEmpty() && !exists(parentPath)) {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(parentPath);
        }
    }
} 