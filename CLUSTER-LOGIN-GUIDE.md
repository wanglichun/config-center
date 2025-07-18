# 集群登录和配置查询指南

## 概述

本指南介绍如何登录到三台机器的集群，并查询拉取的配置信息。

## 1. 启动集群环境

### 1.1 启动单机ZooKeeper

```bash
# 停止集群ZooKeeper（如果正在运行）
docker-compose -f docker-compose-zk-cluster.yml down

# 启动单机ZooKeeper
docker-compose -f docker-compose-zk-single.yml up -d

# 检查ZooKeeper状态
docker ps | grep zookeeper
```

### 1.2 启动配置中心服务

```bash
# 启动Spring Boot应用
mvn spring-boot:run
```

## 2. 登录配置中心

### 2.1 获取登录Token

```bash
# 登录获取token
curl -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123"

# 响应示例：
# {
#   "success": true,
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiJ9...",
#     "userInfo": {
#       "id": 1,
#       "username": "admin",
#       "role": "ADMIN"
#     }
#   }
# }
```

### 2.2 保存Token变量

```bash
# 提取token并保存为环境变量
TOKEN=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

## 3. 注册三台机器到集群

### 3.1 注册机器1

```bash
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-001&instanceIp=192.168.1.1&configKeys=host"
```

### 3.2 注册机器2

```bash
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-002&instanceIp=192.168.1.2&configKeys=host"
```

### 3.3 注册机器3

```bash
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-003&instanceIp=192.168.1.3&configKeys=host"
```

## 4. 创建和发布配置

### 4.1 创建Redis主机配置

```bash
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
```

### 4.2 发布配置

```bash
# 获取配置ID
CONFIG_ID=$(curl -s -X GET "http://localhost:9090/config-center/api/config/page?appName=demo-app&environment=dev&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 发布配置
curl -X POST "http://localhost:9090/config-center/api/config/$CONFIG_ID/publish" \
  -H "Authorization: Bearer $TOKEN"
```

## 5. 查询集群配置

### 5.1 查看订阅机器列表

```bash
curl -X GET "http://localhost:9090/config-center/api/machine-config/subscribers?appName=demo-app&environment=dev&groupName=redis&configKey=host" \
  -H "Authorization: Bearer $TOKEN"
```

### 5.2 查看每台机器的配置

```bash
# 查看机器1的配置
curl -X GET "http://localhost:9090/config-center/api/machine-config/configs/machine-001" \
  -H "Authorization: Bearer $TOKEN"

# 查看机器2的配置
curl -X GET "http://localhost:9090/config-center/api/machine-config/configs/machine-002" \
  -H "Authorization: Bearer $TOKEN"

# 查看机器3的配置
curl -X GET "http://localhost:9090/config-center/api/machine-config/configs/machine-003" \
  -H "Authorization: Bearer $TOKEN"
```

### 5.3 查看ZooKeeper中的配置

```bash
# 查看配置节点
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/configs/demo-app/dev/redis/host

# 查看机器实例节点
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/demo-app/dev/redis/machine-001
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/demo-app/dev/redis/machine-002
docker exec config-center-zookeeper zookeeper-shell localhost:2181 get /config-center/instances/demo-app/dev/redis/machine-003

# 查看通知节点
docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/notifications/demo-app/dev/redis/host
```

## 6. 配置变更和通知

### 6.1 更新配置

```bash
curl -X PUT "http://localhost:9090/config-center/api/config/$CONFIG_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "dev",
    "groupName": "redis",
    "configKey": "host",
    "configValue": "redis-server-02",
    "dataType": "STRING",
    "description": "Redis主机地址"
  }'
```

### 6.2 重新发布配置

```bash
curl -X POST "http://localhost:9090/config-center/api/config/$CONFIG_ID/publish" \
  -H "Authorization: Bearer $TOKEN"
```

### 6.3 查看通知结果

```bash
# 查看通知节点（应该为空，因为机器已经处理了通知）
docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /config-center/notifications/demo-app/dev/redis/host

# 再次查看机器配置（应该已更新）
curl -X GET "http://localhost:9090/config-center/api/machine-config/configs/machine-001" \
  -H "Authorization: Bearer $TOKEN"
```

## 7. 使用前端界面

### 7.1 启动前端

```bash
cd frontend
npm run dev
```

### 7.2 访问前端界面

1. 打开浏览器访问：`http://localhost:3003`
2. 使用用户名：`admin`，密码：`admin123` 登录
3. 进入配置管理页面
4. 查看配置列表和发布状态

## 8. 监控和日志

### 8.1 查看应用日志

```bash
# 查看Spring Boot应用日志
tail -f logs/config-center.log

# 查看ZooKeeper日志
docker logs config-center-zookeeper
```

### 8.2 查看配置变更历史

```bash
# 查看配置历史
curl -X GET "http://localhost:9090/config-center/api/config/$CONFIG_ID/history" \
  -H "Authorization: Bearer $TOKEN"
```

## 9. 故障排查

### 9.1 检查服务状态

```bash
# 检查ZooKeeper连接
docker exec config-center-zookeeper zookeeper-shell localhost:2181 ls /

# 检查配置中心健康状态
curl -X GET "http://localhost:9090/config-center/actuator/health"
```

### 9.2 清理和重置

```bash
# 清理ZooKeeper数据
docker-compose -f docker-compose-zk-single.yml down -v
docker-compose -f docker-compose-zk-single.yml up -d

# 重启配置中心服务
# Ctrl+C 停止服务，然后重新运行 mvn spring-boot:run
```

## 10. 自动化脚本

### 10.1 使用测试脚本

```bash
# 运行完整的三台机器测试
./test-three-machines.sh

# 运行简单配置订阅测试
./test-config-subscription.sh
```

## 总结

通过以上步骤，您可以：

1. **启动集群环境**：单机ZooKeeper + 配置中心服务
2. **登录系统**：获取JWT token进行身份验证
3. **注册机器**：将三台机器注册到集群
4. **创建配置**：创建并发布配置到ZooKeeper
5. **查询配置**：通过API或ZooKeeper CLI查看配置
6. **监控变更**：实时查看配置变更和通知
7. **故障排查**：检查服务状态和日志

这样就完成了三台机器集群的配置订阅和查询功能。 