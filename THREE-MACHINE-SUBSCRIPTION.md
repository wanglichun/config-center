# 三台机器配置订阅架构

## 概述

本文档描述了如何构建一个三台机器的集群，实现配置变更时通知订阅机器拉取最新配置的功能。

## 架构设计

### 1. 系统组件

- **配置中心服务**: Spring Boot应用，负责配置管理和发布
- **ZooKeeper**: 单机ZooKeeper，存储配置和机器实例信息
- **三台机器**: 订阅配置的客户端机器
- **通知机制**: 基于ZooKeeper的临时节点实现配置变更通知

### 2. ZooKeeper节点结构

```
/config-center/
├── configs/                    # 配置存储
│   └── demo-app/
│       └── dev/
│           └── redis/
│               └── host        # Redis主机配置
├── instances/                  # 机器实例信息
│   └── demo-app/
│       └── dev/
│           └── redis/
│               ├── machine-001 # 机器1实例
│               ├── machine-002 # 机器2实例
│               └── machine-003 # 机器3实例
└── notifications/              # 配置变更通知
    └── demo-app/
        └── dev/
            └── redis/
                └── host/
                    ├── machine-001 # 机器1通知
                    ├── machine-002 # 机器2通知
                    └── machine-003 # 机器3通知
```

## 实现步骤

### 1. 启动单机ZooKeeper

```bash
# 停止集群ZooKeeper
docker-compose -f docker-compose-zk-cluster.yml down

# 启动单机ZooKeeper
docker-compose -f docker-compose-zk-single.yml up -d
```

### 2. 启动配置中心服务

```bash
# 启动Spring Boot应用
mvn spring-boot:run
```

### 3. 注册三台机器

每台机器需要注册到配置中心，订阅指定的配置：

```bash
# 注册机器1
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-001&instanceIp=192.168.1.1&configKeys=host"

# 注册机器2
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-002&instanceIp=192.168.1.2&configKeys=host"

# 注册机器3
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJlYWxOYW1lIjoi57O757uf566h55CG5ZGYIiwicm9sZSI6IkFETUlOIiwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc1Mjc0NTgwMiwiZXhwIjoxNzUyODMyMjAyLCJpc3MiOiJjb25maWctY2VudGVyIn0.bUYVLD_Rj4QOTwh9Iy6E_IcttNqbsS7sfLXiJw_uB0fNeYyZRPK4_F0yExCG2MNQePUaRP_nXISdYP75Jf0mug" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-003&instanceIp=192.168.1.3&configKeys=host"
```

### 4. 创建和发布配置

```bash
# 创建Redis主机配置
curl -X POST "http://localhost:9090/config-center/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "dev",
    "groupName": "redis",
    "configKey": "host",
    "configValue": "redis-server-01",
    "dataType": "STRING",
    "description": "Redis主机地址"
  }'

# 发布配置
curl -X POST "http://localhost:9090/config-center/api/config/{configId}/publish" \
  -H "Authorization: Bearer $TOKEN"
```

## 配置变更流程

### 1. 配置发布流程

1. 用户在配置中心修改配置
2. 点击发布按钮
3. 配置中心将配置推送到ZooKeeper
4. 配置中心通知所有订阅该配置的机器
5. 机器收到通知后拉取最新配置并应用

### 2. 通知机制

- 使用ZooKeeper临时节点实现通知
- 每台机器监听自己的通知节点
- 配置变更时创建通知节点
- 机器处理完通知后删除通知节点

### 3. 机器配置订阅流程

1. 机器启动时注册到配置中心
2. 监听配置变更通知
3. 收到通知后拉取最新配置
4. 应用配置变更
5. 定期发送心跳保持连接

## 测试验证

### 1. 运行测试脚本

```bash
# 运行三台机器配置订阅测试
./test-three-machines.sh
```

### 2. 验证步骤

1. **检查机器注册**: 查看ZooKeeper中的机器实例节点
2. **检查配置发布**: 查看ZooKeeper中的配置节点
3. **检查通知机制**: 查看通知节点的创建和删除
4. **检查配置应用**: 查看机器日志确认配置变更

### 3. 查看ZooKeeper数据

```bash
# 查看配置
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host

# 查看机器实例
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/demo-app/dev/redis/machine-001

# 查看通知节点
docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/notifications/demo-app/dev/redis/host
```

## 核心代码

### 1. 机器配置订阅服务

- `MachineConfigSubscriptionService`: 机器配置订阅服务接口
- `MachineConfigSubscriptionServiceImpl`: 实现类，处理机器注册、注销、通知等

### 2. 配置发布服务

- 在`ConfigServiceImpl`中集成通知机制
- 发布配置时自动通知订阅的机器

### 3. 配置订阅客户端

- `ConfigSubscriberDemo`: 简化的配置订阅客户端示例
- 演示如何监听配置变更和通知

## 优势特点

1. **实时性**: 配置变更后立即通知所有订阅机器
2. **可靠性**: 基于ZooKeeper的临时节点，机器下线时自动清理
3. **扩展性**: 支持任意数量的机器订阅
4. **容错性**: 机器重启后可以重新注册和订阅
5. **监控性**: 可以查看所有订阅机器和配置状态

## 注意事项

1. **网络连接**: 确保所有机器能够连接到ZooKeeper
2. **权限控制**: 使用JWT token进行API访问控制
3. **错误处理**: 机器需要处理网络异常和配置拉取失败
4. **性能考虑**: 大量机器订阅时需要考虑ZooKeeper性能
5. **日志记录**: 记录配置变更和通知处理日志便于排查问题 