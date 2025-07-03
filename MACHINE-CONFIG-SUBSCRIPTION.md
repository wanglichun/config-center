# 🔗 机器订阅 ZooKeeper 配置完整指南

## 📋 概述

本文档详细说明机器如何订阅 ZooKeeper 的配置，实现配置的实时监听和动态更新。通过我们提供的配置客户端 SDK，机器可以轻松接入配置中心，实现配置的自动化管理。

## 🏗️ 订阅架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   机器实例1     │    │   机器实例2     │    │   机器实例N     │
│  ConfigClient   │    │  ConfigClient   │    │  ConfigClient   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────┬───────────────────────────────┘
                         │
                         ▼
        ┌─────────────────────────────────────────┐
        │         ZooKeeper 集群                  │
        │  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
        │  │  ZK-1   │ │  ZK-2   │ │  ZK-3   │   │
        │  │ :2181   │ │ :2182   │ │ :2183   │   │
        │  └─────────┘ └─────────┘ └─────────┘   │
        └─────────────────────────────────────────┘
                         │
                         ▼
        ┌─────────────────────────────────────────┐
        │          配置中心服务                   │
        │       (配置发布和管理)                  │
        └─────────────────────────────────────────┘
```

## 🔄 订阅流程详解

### 1. **客户端初始化**

```java
// 创建配置客户端配置
ConfigClientConfig config = new ConfigClientConfig()
    .appName("user-service")                    // 应用名称
    .environment("prod")                        // 环境
    .groupName("database")                      // 分组
    .zkConnectionString("localhost:2181,localhost:2182,localhost:2183")
    .subscribeConfigs(                          // 订阅的配置键
        "database.url",
        "database.password",
        "redis.cluster.nodes"
    )
    .heartbeatInterval(30000);                  // 心跳间隔

// 创建并启动客户端
ConfigClient client = new ConfigClient(config);
client.start();
```

### 2. **ZooKeeper 连接建立**

客户端启动时会：
- 🔗 连接到 ZooKeeper 集群
- ⏱️ 设置会话超时和重试策略
- 🔄 等待连接建立成功

```java
// 使用 Curator 框架建立连接
CuratorFramework client = CuratorFrameworkFactory.newClient(
    zkConnectionString,
    sessionTimeout,
    connectionTimeout,
    new ExponentialBackoffRetry(retryInterval, retryTimes)
);
```

### 3. **机器实例注册**

连接成功后，客户端会在 ZooKeeper 中注册机器信息：

```
/config-center/
├── user-service/
│   ├── prod/
│   │   ├── machines/
│   │   │   ├── user-service-192.168.1.100-1672531200000  # 机器注册节点
│   │   │   └── user-service-192.168.1.101-1672531201000
│   │   ├── subscriptions/
│   │   │   ├── database.url/
│   │   │   │   ├── user-service-192.168.1.100-1672531200000  # 配置订阅节点
│   │   │   │   └── user-service-192.168.1.101-1672531201000
│   │   │   └── database.password/
│   │   │       ├── user-service-192.168.1.100-1672531200000
│   │   │       └── user-service-192.168.1.101-1672531201000
│   │   └── heartbeats/
│   │       ├── user-service-192.168.1.100-1672531200000  # 心跳节点
│   │       └── user-service-192.168.1.101-1672531201000
```

### 4. **配置订阅监听**

客户端使用 `TreeCache` 监听配置路径的变化：

```java
// 创建配置监听器
TreeCache treeCache = new TreeCache(client, configPath);
treeCache.getListenable().addListener(new TreeCacheListener() {
    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) {
        handleConfigChangeEvent(event);  // 处理配置变更
    }
});
treeCache.start();
```

### 5. **配置变更处理**

当配置发生变更时，客户端会：
- 📝 更新本地配置缓存
- 🔔 触发配置变更监听器
- 📊 记录变更日志

```java
// 处理配置变更事件
switch (event.getType()) {
    case NODE_ADDED:
    case NODE_UPDATED:
        String newValue = new String(event.getData().getData());
        String oldValue = localCache.put(configKey, newValue);
        
        // 触发业务监听器
        ConfigChangeListener listener = listeners.get(configKey);
        if (listener != null) {
            listener.onConfigChanged(configKey, oldValue, newValue);
        }
        break;
}
```

## 🛠️ 使用示例

### 1. **基础使用**

```java
public class UserService {
    private ConfigClient configClient;
    private DataSource dataSource;
    
    public void init() throws Exception {
        // 初始化配置客户端
        ConfigClientConfig config = new ConfigClientConfig()
            .appName("user-service")
            .environment("prod")
            .subscribeConfigs("database.url", "database.password");
            
        configClient = new ConfigClient(config);
        
        // 添加数据库配置监听器
        configClient.addConfigChangeListener("database.url", this::onDatabaseConfigChanged);
        configClient.addConfigChangeListener("database.password", this::onDatabaseConfigChanged);
        
        // 启动客户端
        configClient.start();
        
        // 初始化数据源
        initDataSource();
    }
    
    private void onDatabaseConfigChanged(String key, String oldValue, String newValue) {
        log.info("数据库配置变更: {} = {}", key, newValue);
        // 重新初始化数据源
        initDataSource();
    }
    
    private void initDataSource() {
        String url = configClient.getConfig("database.url");
        String password = configClient.getConfig("database.password");
        // 创建数据源...
    }
}
```

### 2. **Spring Boot 集成**

```java
@Configuration
@EnableConfigurationProperties(ConfigCenterProperties.class)
public class ConfigCenterAutoConfiguration {
    
    @Bean
    public ConfigClient configClient(ConfigCenterProperties properties) throws Exception {
        ConfigClientConfig config = new ConfigClientConfig()
            .appName(properties.getAppName())
            .environment(properties.getEnvironment())
            .zkConnectionString(properties.getZkConnectionString())
            .subscribeConfigs(properties.getSubscribedConfigs());
            
        ConfigClient client = new ConfigClient(config);
        client.start();
        return client;
    }
    
    @Bean
    @RefreshScope  // 支持配置动态刷新
    public DataSource dataSource(ConfigClient configClient) {
        String url = configClient.getConfig("database.url");
        String username = configClient.getConfig("database.username");
        String password = configClient.getConfig("database.password");
        
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
```

### 3. **配置类型转换**

```java
// 字符串配置
String dbUrl = configClient.getConfig("database.url", "default-url");

// 数字配置
Integer maxPoolSize = configClient.getConfig("database.maxPoolSize", Integer.class);

// 布尔配置
Boolean cacheEnabled = configClient.getConfig("cache.enabled", Boolean.class);

// JSON 对象配置
RedisConfig redisConfig = configClient.getConfig("redis.config", RedisConfig.class);

// 列表配置
List<String> servers = configClient.getConfig("servers", List.class);
```

## 🔒 安全与可靠性

### 1. **连接容错**

- **重试机制**: 连接失败时自动重试，使用指数退避策略
- **会话恢复**: ZooKeeper 会话过期时自动重新连接
- **集群支持**: 支持 ZooKeeper 集群，单节点故障不影响服务

### 2. **数据一致性**

- **本地缓存**: 配置加载到本地缓存，即使 ZooKeeper 短暂不可用也能正常工作
- **原子更新**: 配置变更以原子操作方式更新本地缓存
- **版本控制**: 支持配置版本管理，避免并发更新冲突

### 3. **监控与告警**

- **心跳机制**: 定期发送心跳，监控客户端存活状态
- **健康检查**: 提供健康检查接口，方便运维监控
- **变更日志**: 详细记录所有配置变更操作

## 📊 监控指标

### 1. **客户端指标**

```java
// 获取客户端状态
boolean isHealthy = configClient.isStarted();
int configCount = configClient.getAllConfigs().size();
String instanceId = configClient.getInstanceInfo().getInstanceId();

// 监控配置变更频率
@EventListener
public void onConfigChange(ConfigChangeEvent event) {
    meterRegistry.counter("config.change", 
        "key", event.getKey(),
        "app", event.getAppName()).increment();
}
```

### 2. **系统指标**

- **连接数**: 当前连接到 ZooKeeper 的客户端数量
- **配置订阅数**: 每个配置的订阅客户端数量
- **变更频率**: 配置变更的频率统计
- **响应时间**: 配置变更到客户端接收的时间延迟

## 🚀 最佳实践

### 1. **配置设计**

- **粒度控制**: 合理设计配置粒度，避免过于细碎或过于粗糙
- **命名规范**: 使用清晰的配置命名规范，如 `module.component.property`
- **类型安全**: 尽量使用强类型配置，避免类型转换错误

### 2. **性能优化**

- **批量订阅**: 一次性订阅多个相关配置，减少网络开销
- **本地缓存**: 充分利用本地缓存，减少对 ZooKeeper 的频繁访问
- **异步处理**: 配置变更处理使用异步方式，避免阻塞主流程

### 3. **故障处理**

- **优雅降级**: ZooKeeper 不可用时，使用本地缓存或默认配置
- **重试策略**: 设置合理的重试次数和间隔，避免雪崩效应
- **监控告警**: 及时发现和处理配置相关的问题

## 🎯 使用流程总结

1. **📦 引入 SDK**: 将配置客户端 SDK 集成到项目中
2. **⚙️ 配置客户端**: 设置应用信息、ZooKeeper 地址、订阅配置
3. **🚀 启动客户端**: 调用 `start()` 方法启动配置订阅
4. **👂 添加监听器**: 为关键配置添加变更监听器
5. **📖 读取配置**: 使用 `getConfig()` 方法读取配置值
6. **🔄 响应变更**: 在监听器中处理配置变更逻辑
7. **💓 健康监控**: 监控客户端状态和配置变更情况
8. **🛑 优雅关闭**: 应用关闭时调用 `stop()` 方法清理资源

通过这套完整的配置订阅方案，机器可以实现：
- ✅ **实时配置更新**: 配置变更立即生效
- ✅ **高可用性**: 支持 ZooKeeper 集群和故障恢复
- ✅ **类型安全**: 强类型配置读取和转换
- ✅ **监控完善**: 全面的监控和告警机制
- ✅ **易于集成**: 简单的 API 和 Spring Boot 自动配置 