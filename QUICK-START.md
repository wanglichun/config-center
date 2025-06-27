# 🚀 ZooKeeper集群配置中心快速上手指南

## 📖 概述

本项目演示如何构建一个基于ZooKeeper集群的分布式配置中心，实现：
- **配置集中管理**: 统一管理所有应用的配置
- **实时推送**: 配置变更立即推送到订阅的客户端
- **高可用集群**: 3节点ZooKeeper集群保证服务可用性
- **动态更新**: 应用无需重启即可获取最新配置

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ZooKeeper-1   │    │   ZooKeeper-2   │    │   ZooKeeper-3   │
│   (Leader)      │◄──►│   (Follower)    │◄──►│   (Follower)    │
│   Port: 2181    │    │   Port: 2182    │    │   Port: 2183    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │ 配置订阅/推送
                    ┌─────────────────┐
                    │  Config Center  │
                    │   Application   │ 
                    │   (演示客户端)   │
                    └─────────────────┘
```

## 🛠️ 环境准备

### 系统要求
- **Java**: JDK 8 或更高版本
- **Docker**: 用于运行ZooKeeper集群
- **Docker Compose**: 用于容器编排
- **Maven**: 用于构建Java项目

### 检查环境
```bash
# 检查Java版本
java -version

# 检查Docker
docker --version
docker-compose --version

# 检查Maven
mvn --version
```

## 🚀 快速启动

### 步骤1: 启动ZooKeeper集群

```bash
# 使用便捷脚本启动集群
./start-zk-cluster.sh start

# 或者手动启动
docker-compose -f docker-compose-zk-cluster.yml up -d
```

### 步骤2: 验证集群状态

```bash
# 检查集群状态
./start-zk-cluster.sh status

# 查看容器运行情况
docker-compose -f docker-compose-zk-cluster.yml ps
```

预期输出：
```
[SUCCESS] zk1 容器正在运行
[SUCCESS] zk2 容器正在运行  
[SUCCESS] zk3 容器正在运行
[SUCCESS] zk1 端口 2181 可达
[SUCCESS] zk1 ZooKeeper状态: leader
[SUCCESS] zk2 ZooKeeper状态: follower
[SUCCESS] zk3 ZooKeeper状态: follower
```

### 步骤3: 启动配置中心应用

```bash
# 编译项目
mvn clean compile

# 启动应用（会自动运行演示）
mvn spring-boot:run
```

## 🎮 交互式演示

应用启动后，你会看到交互式命令界面：

```
🎮 进入交互模式，可以手动修改配置进行测试
命令格式:
  update <key> <value> - 更新配置
  get <key> - 获取配置
  list - 列出所有配置
  demo - 运行自动演示
  help - 显示帮助
  exit - 退出程序

> 
```

### 基础命令演示

```bash
# 查看所有配置
> list

# 获取特定配置
> get database.url

# 更新配置（会实时推送给所有订阅者）
> update database.password newPassword123

# 运行自动演示（展示各种配置变更场景）
> demo
```

## 🔍 监控配置变更

当你修改配置时，会看到实时的变更通知：

```
🛠️ 更新配置: database.password = newPassword123
🔄 配置更新: database.password = newPassword123 (原值: 123456)
🔔 触发配置变更回调: operation=UPDATED, key=database.password, value=newPassword123
📊 数据库配置发生变更，需要重新初始化数据源
```

## 🎭 演示场景

### 场景1: 数据库配置更新
```bash
> update database.password newPassword123
> update database.url jdbc:mysql://newhost:3306/demo
```
**效果**: 模拟数据库连接参数变更，触发连接池重新初始化

### 场景2: 服务配置调整
```bash
> update server.port 8081
> update logging.level DEBUG
```
**效果**: 模拟服务运行参数调整，无需重启服务

### 场景3: 功能开关
```bash
> update feature.newFeature enabled
> update cache.enabled false
```
**效果**: 模拟功能开关和缓存策略调整

### 场景4: 批量配置回滚
```bash
> update database.password 123456
> update server.port 8080
> update logging.level INFO
```
**效果**: 模拟配置回滚操作

## 🔧 ZooKeeper客户端操作

### 使用内置Shell
```bash
# 连接到ZooKeeper Shell
./start-zk-cluster.sh shell

# 在Shell中执行命令
ls /                                    # 查看根目录
ls /config-center                      # 查看配置目录
ls /config-center/demo-app/dev          # 查看应用配置
get /config-center/demo-app/dev/database.url  # 获取配置值
```

### 手动创建配置
```bash
# 创建新配置路径
create -p /config-center/my-app/prod

# 设置配置值
create /config-center/my-app/prod/app.name "My Application"
create /config-center/my-app/prod/app.version "1.0.0"

# 修改配置值
set /config-center/demo-app/dev/database.timeout 5000
```

## 📊 配置路径结构

```
/config-center/                    # 配置根目录
├── demo-app/                      # 应用名称
│   ├── dev/                       # 环境：开发
│   │   ├── database.url          # 数据库连接地址
│   │   ├── database.username     # 数据库用户名
│   │   ├── database.password     # 数据库密码
│   │   ├── server.port           # 服务端口
│   │   ├── logging.level         # 日志级别
│   │   ├── cache.enabled         # 缓存开关
│   │   └── cache.timeout         # 缓存超时时间
│   ├── test/                     # 环境：测试
│   └── prod/                     # 环境：生产
└── other-app/                    # 其他应用
    ├── dev/
    ├── test/
    └── prod/
```

## 🔍 故障排除

### 常见问题1: ZooKeeper连接失败
```bash
# 检查集群状态
./start-zk-cluster.sh status

# 查看容器日志
./start-zk-cluster.sh logs

# 重启集群
./start-zk-cluster.sh restart
```

### 常见问题2: 配置不生效
```bash
# 验证配置路径
./start-zk-cluster.sh shell
> ls /config-center/demo-app/dev
> get /config-center/demo-app/dev/your-config-key

# 检查应用日志
tail -f logs/config-center.log
```

### 常见问题3: 端口冲突
如果端口被占用，修改 `docker-compose-zk-cluster.yml` 中的端口映射：
```yaml
ports:
  - "12181:2181"  # 修改本地端口
  - "12182:2181" 
  - "12183:2181"
```

然后更新 `ConfigSubscriber.java` 中的连接字符串：
```java
private static final String ZK_CONNECTION_STRING = "localhost:12181,localhost:12182,localhost:12183";
```

## 🛠️ 集群管理

### 启动/停止集群
```bash
./start-zk-cluster.sh start    # 启动集群
./start-zk-cluster.sh stop     # 停止集群
./start-zk-cluster.sh restart  # 重启集群
```

### 数据备份与恢复
```bash
# 备份ZooKeeper数据
docker exec zk1 tar -czf /tmp/zk-backup.tar.gz /var/lib/zookeeper/data
docker cp zk1:/tmp/zk-backup.tar.gz ./backup/

# 清理集群数据（慎用！）
./start-zk-cluster.sh cleanup
```

### 集群扩容
修改 `docker-compose-zk-cluster.yml` 添加新节点：
```yaml
zk4:
  image: confluentinc/cp-zookeeper:7.4.0
  hostname: zk4
  container_name: zk4
  ports:
    - "2184:2181"
  environment:
    ZOOKEEPER_SERVER_ID: 4
    ZOOKEEPER_SERVERS: zk1:2888:3888;zk2:2888:3888;zk3:2888:3888;zk4:2888:3888
```

## 📈 性能监控

### 查看集群状态
```bash
# 查看详细状态信息
docker exec zk1 /bin/bash -c "echo 'stat' | nc localhost 2181"
docker exec zk1 /bin/bash -c "echo 'conf' | nc localhost 2181"
docker exec zk1 /bin/bash -c "echo 'ruok' | nc localhost 2181"
```

### 监控连接数
```bash
# 查看客户端连接
docker exec zk1 /bin/bash -c "echo 'cons' | nc localhost 2181"
```

## 🔒 安全配置

### 启用ACL认证
在ZooKeeper中设置访问控制：
```bash
# 进入ZooKeeper Shell
./start-zk-cluster.sh shell

# 设置ACL
setAcl /config-center world:anyone:cdrwa
addauth digest admin:password123
create /secure-config "sensitive data" digest:admin:password123:cdrwa
```

### 配置SSL/TLS
修改 `docker-compose-zk-cluster.yml` 添加SSL配置：
```yaml
environment:
  ZOOKEEPER_SSL_CLIENT_ENABLE: 'true'
  ZOOKEEPER_SERVER_CNXN_FACTORY: org.apache.zookeeper.server.NettyServerCnxnFactory
```

## 🎯 最佳实践

### 1. 配置命名规范
```
应用.模块.配置项
例如: order-service.database.connection-timeout
     user-service.cache.expire-time
```

### 2. 环境隔离
```
/config-center/
├── {app-name}/
│   ├── local/     # 本地开发环境
│   ├── dev/       # 开发环境
│   ├── test/      # 测试环境
│   ├── staging/   # 预发布环境
│   └── prod/      # 生产环境
```

### 3. 配置分组
```
/config-center/{app-name}/{env}/
├── database/      # 数据库相关配置
├── cache/         # 缓存相关配置
├── security/      # 安全相关配置
├── feature/       # 功能开关
└── business/      # 业务配置
```

### 4. 敏感信息处理
- 数据库密码等敏感信息单独管理
- 使用配置加密/解密机制
- 设置合适的ACL权限控制

## 🔗 相关资源

- **Apache ZooKeeper**: https://zookeeper.apache.org/
- **Apache Curator**: http://curator.apache.org/
- **Spring Cloud Config**: https://spring.io/projects/spring-cloud-config
- **Docker Compose**: https://docs.docker.com/compose/

## 📝 下一步

1. **集成到现有项目**: 将ConfigSubscriber集成到你的Spring Boot应用中
2. **添加配置验证**: 实现配置格式验证和业务规则检查
3. **配置版本管理**: 实现配置变更历史和回滚功能
4. **监控告警**: 添加配置变更监控和异常告警
5. **Web管理界面**: 开发可视化的配置管理界面

祝你使用愉快！🎉 