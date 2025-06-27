# ZooKeeper集群配置中心演示

这个项目演示如何构建一个基于ZooKeeper的分布式配置中心，实现配置的集中管理和实时推送。

## 🏗️ 架构概述

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ZooKeeper-1   │    │   ZooKeeper-2   │    │   ZooKeeper-3   │
│   (Leader)      │◄──►│   (Follower)    │◄──►│   (Follower)    │
│   Port: 2181    │    │   Port: 2182    │    │   Port: 2183    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Config Center  │
                    │   Application   │
                    │   Port: 9090    │
                    └─────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App 1  │    │   Client App 2  │    │   Client App N  │
│   (Subscriber)  │    │   (Subscriber)  │    │   (Subscriber)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 快速开始

### 1. 启动ZooKeeper集群

```bash
# 启动3节点ZooKeeper集群
docker-compose -f docker-compose-zk-cluster.yml up -d

# 查看集群状态
docker-compose -f docker-compose-zk-cluster.yml ps

# 查看日志
docker-compose -f docker-compose-zk-cluster.yml logs -f
```

### 2. 验证集群状态

```bash
# 连接到ZooKeeper集群验证
docker exec -it zk1 /bin/bash -c "echo 'stat' | nc localhost 2181"
docker exec -it zk2 /bin/bash -c "echo 'stat' | nc localhost 2181"
docker exec -it zk3 /bin/bash -c "echo 'stat' | nc localhost 2181"

# 查看集群角色
docker exec -it zk1 /bin/bash -c "echo 'stat' | nc localhost 2181 | grep Mode"
docker exec -it zk2 /bin/bash -c "echo 'stat' | nc localhost 2181 | grep Mode"
docker exec -it zk3 /bin/bash -c "echo 'stat' | nc localhost 2181 | grep Mode"
```

### 3. 启动配置中心应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 4. 运行配置订阅演示

应用启动后，`ConfigSubscriberDemo` 会自动运行，展示配置订阅功能。

## 📋 功能特性

### 🔧 配置管理
- **实时同步**: 配置变更立即推送到所有订阅客户端
- **高可用**: 基于ZooKeeper集群，单点故障自动恢复
- **版本控制**: 配置变更历史追踪
- **命名空间**: 支持多环境配置隔离

### 🎮 交互式演示
- **手动更新**: 通过命令行实时修改配置
- **自动演示**: 预设场景自动演示配置变更流程
- **实时监控**: 定期显示当前配置状态

## 🛠️ 使用指南

### 命令行交互

启动应用后，进入交互模式：

```bash
> help                              # 显示帮助信息
> list                              # 列出所有配置
> get database.url                  # 获取特定配置
> update database.password newpass  # 更新配置
> demo                              # 运行自动演示
> exit                              # 退出程序
```

### 配置路径结构

```
/config-center/
├── demo-app/
│   ├── dev/
│   │   ├── database.url
│   │   ├── database.username
│   │   ├── database.password
│   │   ├── server.port
│   │   ├── logging.level
│   │   ├── cache.enabled
│   │   └── cache.timeout
│   ├── test/
│   └── prod/
└── other-app/
    ├── dev/
    ├── test/
    └── prod/
```

## 🔍 监控和调试

### ZooKeeper客户端工具

```bash
# 使用ZooKeeper命令行客户端
docker exec -it zk1 /opt/kafka/bin/zookeeper-shell.sh localhost:2181

# 在ZooKeeper shell中执行命令
ls /                                # 列出根目录
ls /config-center                  # 列出配置中心目录
get /config-center/demo-app/dev/database.url  # 获取配置值
```

### 日志监控

```bash
# 查看应用日志
tail -f logs/config-center.log

# 查看ZooKeeper日志
docker-compose -f docker-compose-zk-cluster.yml logs -f zk1
```

## 🎭 演示场景

### 场景1：数据库配置变更
```bash
> update database.password newPassword123
```
- 触发数据库连接池重新初始化
- 所有订阅客户端立即收到变更通知

### 场景2：服务端口变更
```bash
> update server.port 8081
```
- 触发Web服务器重启逻辑
- 实现零停机配置更新

### 场景3：日志级别动态调整
```bash
> update logging.level DEBUG
```
- 动态调整应用日志输出级别
- 无需重启应用即可生效

### 场景4：功能开关
```bash
> update feature.newFeature enabled
```
- 动态启用/禁用应用功能
- 实现灰度发布和A/B测试

## 🔧 集成到现有项目

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>5.4.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>5.4.0</version>
</dependency>
```

### 2. 配置客户端

```java
@Configuration
public class ZooKeeperConfig {
    
    @Bean
    public ConfigSubscriber configSubscriber() {
        ConfigSubscriber subscriber = new ConfigSubscriber();
        subscriber.start();
        return subscriber;
    }
}
```

### 3. 使用配置

```java
@Service
public class BusinessService {
    
    @Autowired
    private ConfigSubscriber configSubscriber;
    
    public void doSomething() {
        String dbUrl = configSubscriber.getConfig("database.url");
        String cacheEnabled = configSubscriber.getConfig("cache.enabled", "true");
        // 使用配置...
    }
}
```

## 📊 性能特性

- **连接复用**: 单个ZooKeeper连接支持多个配置监听
- **批量更新**: 支持配置批量变更和事务性更新
- **本地缓存**: 客户端本地缓存减少网络开销
- **故障恢复**: 网络中断后自动重连和状态同步

## 🔒 安全特性

- **ACL控制**: ZooKeeper原生ACL权限控制
- **连接认证**: 客户端连接认证机制
- **配置加密**: 敏感配置自动加密存储
- **审计日志**: 完整的配置变更审计跟踪

## 🚨 故障处理

### ZooKeeper节点故障
- 集群自动选举新的Leader
- 客户端自动重连到可用节点
- 配置数据不会丢失

### 网络分区
- 客户端本地缓存保证服务可用性
- 网络恢复后自动同步最新配置
- 冲突解决机制

### 配置回滚
```bash
# 手动回滚配置
> update database.password originalPassword
> update logging.level INFO
```

## 📈 扩展功能

### 配置模板
- 支持配置模板和变量替换
- 环境特定配置继承
- 配置验证和格式检查

### 配置推送策略
- 立即推送（默认）
- 定时批量推送
- 手动触发推送

### 集成其他系统
- Spring Cloud Config集成
- Kubernetes ConfigMap同步
- 配置管理平台对接

## 🔧 调优建议

### ZooKeeper集群调优
```yaml
# 在docker-compose-zk-cluster.yml中调整
environment:
  ZOOKEEPER_TICK_TIME: 2000
  ZOOKEEPER_INIT_LIMIT: 10
  ZOOKEEPER_SYNC_LIMIT: 5
  ZOOKEEPER_MAX_CLIENT_CNXNS: 1000
```

### 客户端调优
```java
// 连接参数调优
CuratorFramework client = CuratorFrameworkFactory.newClient(
    connectionString,
    new ExponentialBackoffRetry(1000, 3)
);
```

## 💡 最佳实践

1. **配置命名规范**: 使用层次化命名，如 `app.module.config`
2. **环境隔离**: 不同环境使用不同的命名空间
3. **敏感信息**: 数据库密码等敏感信息单独管理
4. **版本控制**: 重要配置变更前备份
5. **监控告警**: 配置变更和客户端连接状态监控

## 🔗 相关资源

- [Apache ZooKeeper官方文档](https://zookeeper.apache.org/)
- [Apache Curator官方文档](http://curator.apache.org/)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Nacos配置中心](https://nacos.io/)

## 🐛 故障排除

### 常见问题

1. **连接超时**
   ```bash
   # 检查ZooKeeper集群状态
   docker-compose -f docker-compose-zk-cluster.yml ps
   ```

2. **配置不生效**
   ```bash
   # 检查配置路径是否正确
   > get your.config.key
   ```

3. **客户端断连**
   ```bash
   # 查看客户端日志
   tail -f logs/config-center.log | grep -i "connection"
   ```

### 日志分析

```bash
# 查看ZooKeeper选举日志
docker exec -it zk1 cat /var/lib/zookeeper/log/zookeeper.log

# 查看客户端连接日志
grep "Connected" logs/config-center.log
``` 