package com.example.configcenter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * ZooKeeper配置类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.connect-string}")
    private String connectString;

    @Value("${zookeeper.session-timeout}")
    private int sessionTimeout;

    @Value("${zookeeper.connection-timeout}")
    private int connectionTimeout;

    @Value("${zookeeper.retry-times}")
    private int retryTimes;

    @Value("${zookeeper.sleep-between-retry}")
    private int sleepBetweenRetry;

    @Value("${zookeeper.namespace}")
    private String namespace;

    private CuratorFramework curatorFramework;

    /**
     * 创建CuratorFramework实例
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(sleepBetweenRetry, retryTimes);
        
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();

        // 添加连接状态监听器
        curatorFramework.getConnectionStateListenable().addListener((client, newState) -> {
            log.info("ZooKeeper连接状态变更: {}", newState);
        });

        log.info("ZooKeeper客户端初始化完成: {}", connectString);
        return curatorFramework;
    }

    /**
     * 创建路径监听缓存
     */
    @Bean
    public PathChildrenCache pathChildrenCache(CuratorFramework curatorFramework) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, "/configs", true);
        
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("配置节点添加: {}", event.getData().getPath());
                        break;
                    case CHILD_UPDATED:
                        log.info("配置节点更新: {}", event.getData().getPath());
                        break;
                    case CHILD_REMOVED:
                        log.info("配置节点删除: {}", event.getData().getPath());
                        break;
                    default:
                        break;
                }
            }
        });
        
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        log.info("ZooKeeper路径监听器启动完成");
        return pathChildrenCache;
    }

    @PreDestroy
    public void destroy() {
        if (curatorFramework != null) {
            curatorFramework.close();
            log.info("ZooKeeper客户端已关闭");
        }
    }
} 