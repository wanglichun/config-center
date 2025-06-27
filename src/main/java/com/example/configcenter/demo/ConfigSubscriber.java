package com.example.configcenter.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 配置订阅客户端演示
 * 演示如何订阅ZooKeeper上的配置变更并实时更新本地配置
 */
@Component
public class ConfigSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(ConfigSubscriber.class);
    
    private static final String ZK_CONNECTION_STRING = "localhost:2181,localhost:2182,localhost:2183";
    private static final String CONFIG_ROOT_PATH = "/config-center";
    private static final String APP_NAME = "demo-app";
    private static final String ENVIRONMENT = "dev";
    
    private CuratorFramework client;
    private final Map<String, String> localConfig = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private TreeCache treeCache;

    /**
     * 初始化配置订阅客户端
     */
    public void start() throws Exception {
        logger.info("开始启动配置订阅客户端...");
        
        // 初始化ZooKeeper客户端
        initZooKeeperClient();
        
        // 创建配置路径和初始配置
        setupInitialConfig();
        
        // 启动配置监听
        startConfigWatcher();
        
        logger.info("配置订阅客户端启动完成");
    }
    
    /**
     * 初始化ZooKeeper客户端
     */
    private void initZooKeeperClient() throws Exception {
        client = CuratorFrameworkFactory.newClient(
            ZK_CONNECTION_STRING,
            new ExponentialBackoffRetry(1000, 3)
        );
        
        client.start();
        
        // 等待连接建立
        if (!client.blockUntilConnected(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("无法连接到ZooKeeper集群");
        }
        
        logger.info("成功连接到ZooKeeper集群: {}", ZK_CONNECTION_STRING);
    }
    
    /**
     * 创建配置路径和初始配置
     */
    private void setupInitialConfig() throws Exception {
        String appConfigPath = String.format("%s/%s/%s", CONFIG_ROOT_PATH, APP_NAME, ENVIRONMENT);
        
        // 确保路径存在
        if (client.checkExists().forPath(appConfigPath) == null) {
            client.create()
                .creatingParentsIfNeeded()
                .forPath(appConfigPath);
        }
        
        // 创建一些初始配置
        Map<String, Object> initialConfigs = new HashMap<>();
        initialConfigs.put("database.url", "jdbc:mysql://localhost:3306/demo");
        initialConfigs.put("database.username", "root");
        initialConfigs.put("database.password", "123456");
        initialConfigs.put("server.port", "8080");
        initialConfigs.put("logging.level", "INFO");
        initialConfigs.put("cache.enabled", true);
        initialConfigs.put("cache.timeout", 300);
        
        for (Map.Entry<String, Object> entry : initialConfigs.entrySet()) {
            String configPath = appConfigPath + "/" + entry.getKey();
            String configValue = objectMapper.writeValueAsString(entry.getValue());
            
            if (client.checkExists().forPath(configPath) == null) {
                client.create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(configPath, configValue.getBytes(StandardCharsets.UTF_8));
                logger.info("创建初始配置: {} = {}", entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 启动配置监听器
     */
    private void startConfigWatcher() throws Exception {
        String appConfigPath = String.format("%s/%s/%s", CONFIG_ROOT_PATH, APP_NAME, ENVIRONMENT);
        
        // 使用TreeCache监听整个配置树的变化
        treeCache = new TreeCache(client, appConfigPath);
        
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                handleConfigChange(event);
            }
        });
        
        // 启动缓存
        treeCache.start();
        
        // 加载初始配置到本地缓存
        loadInitialConfig(appConfigPath);
        
        logger.info("配置监听器已启动，监听路径: {}", appConfigPath);
    }
    
    /**
     * 加载初始配置到本地缓存
     */
    private void loadInitialConfig(String configPath) throws Exception {
        if (client.checkExists().forPath(configPath) != null) {
            for (String child : client.getChildren().forPath(configPath)) {
                String childPath = configPath + "/" + child;
                byte[] data = client.getData().forPath(childPath);
                if (data != null) {
                    String value = new String(data, StandardCharsets.UTF_8);
                    // 去掉JSON字符串的引号
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    localConfig.put(child, value);
                    logger.info("加载初始配置: {} = {}", child, value);
                }
            }
        }
    }
    
    /**
     * 处理配置变更事件
     */
    private void handleConfigChange(TreeCacheEvent event) {
        try {
            String path = event.getData() != null ? event.getData().getPath() : "null";
            String configKey = path.substring(path.lastIndexOf('/') + 1);
            
            switch (event.getType()) {
                case NODE_ADDED:
                    if (event.getData().getData() != null) {
                        String value = new String(event.getData().getData(), StandardCharsets.UTF_8);
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        localConfig.put(configKey, value);
                        logger.info("🆕 配置新增: {} = {}", configKey, value);
                        onConfigChanged(configKey, value, "ADDED");
                    }
                    break;
                    
                case NODE_UPDATED:
                    if (event.getData().getData() != null) {
                        String newValue = new String(event.getData().getData(), StandardCharsets.UTF_8);
                        if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
                            newValue = newValue.substring(1, newValue.length() - 1);
                        }
                        String oldValue = localConfig.put(configKey, newValue);
                        logger.info("🔄 配置更新: {} = {} (原值: {})", configKey, newValue, oldValue);
                        onConfigChanged(configKey, newValue, "UPDATED");
                    }
                    break;
                    
                case NODE_REMOVED:
                    String removedValue = localConfig.remove(configKey);
                    logger.info("🗑️ 配置删除: {} (原值: {})", configKey, removedValue);
                    onConfigChanged(configKey, null, "REMOVED");
                    break;
                    
                default:
                    // 忽略其他事件类型
                    break;
            }
        } catch (Exception e) {
            logger.error("处理配置变更事件失败", e);
        }
    }
    
    /**
     * 配置变更回调
     */
    private void onConfigChanged(String key, String value, String operation) {
        logger.info("🔔 触发配置变更回调: operation={}, key={}, value={}", operation, key, value);
        
        // 模拟不同配置的处理逻辑
        switch (key) {
            case "database.url":
            case "database.username":
            case "database.password":
                logger.info("📊 数据库配置发生变更，需要重新初始化数据源");
                break;
            case "server.port":
                logger.info("🌐 服务端口配置发生变更");
                break;
            case "logging.level":
                logger.info("📝 日志级别配置发生变更");
                break;
            default:
                logger.info("⚙️ 配置发生变更: {}", key);
                break;
        }
    }
    
    /**
     * 获取配置值
     */
    public String getConfig(String key) {
        return localConfig.get(key);
    }
    
    /**
     * 获取配置值（带默认值）
     */
    public String getConfig(String key, String defaultValue) {
        return localConfig.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取所有配置
     */
    public Map<String, String> getAllConfig() {
        return new HashMap<>(localConfig);
    }
    
    /**
     * 更新配置（用于测试）
     */
    public void updateConfig(String key, String value) throws Exception {
        String appConfigPath = String.format("%s/%s/%s", CONFIG_ROOT_PATH, APP_NAME, ENVIRONMENT);
        String configPath = appConfigPath + "/" + key;
        String jsonValue = objectMapper.writeValueAsString(value);
        client.setData().forPath(configPath, jsonValue.getBytes(StandardCharsets.UTF_8));
        logger.info("🛠️ 更新配置: {} = {}", key, value);
    }
    
    /**
     * 关闭客户端
     */
    public void close() throws Exception {
        if (treeCache != null) {
            treeCache.close();
        }
        if (client != null) {
            client.close();
        }
        logger.info("配置订阅客户端已关闭");
    }
} 