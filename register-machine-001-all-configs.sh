#!/bin/bash

# 注册001机器订阅demo-app/dev下的所有配置
# 包含：common组、database组、redis组的所有配置

echo "=== 注册001机器订阅所有配置 ==="

# 获取JWT token
echo "获取JWT token..."
TOKEN=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "获取token失败"
    exit 1
fi

echo "Token获取成功"

# 1. 注册common组配置订阅
echo "注册common组配置订阅..."
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=common&instanceId=machine-001&instanceIp=192.168.1.101&configKeys=app.version&configKeys=debug.enabled"

echo -e "\n"

# 2. 注册database组配置订阅
echo "注册database组配置订阅..."
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=database&instanceId=machine-001&instanceIp=192.168.1.101&configKeys=jdbc.url&configKeys=jdbc.username"

echo -e "\n"

# 3. 注册redis组配置订阅
echo "注册redis组配置订阅..."
curl -X POST "http://localhost:9090/config-center/api/machine-config/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Bearer $TOKEN" \
  -d "appName=demo-app&environment=dev&groupName=redis&instanceId=machine-001&instanceIp=192.168.1.101&configKeys=host"

echo -e "\n"

# 4. 验证注册结果
echo "验证注册结果..."
echo "获取machine-001的配置信息："
curl -X GET "http://localhost:9090/config-center/api/machine-config/configs/machine-001" \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n"

# 5. 查看ZooKeeper中的注册情况
echo "查看ZooKeeper中的注册情况："
echo "common组："
docker exec zk1 zookeeper-shell localhost:2181 ls /config-center/instances/demo-app/dev/common 2>/dev/null

echo "database组："
docker exec zk1 zookeeper-shell localhost:2181 ls /config-center/instances/demo-app/dev/database 2>/dev/null

echo "redis组："
docker exec zk1 zookeeper-shell localhost:2181 ls /config-center/instances/demo-app/dev/redis 2>/dev/null

echo -e "\n=== 注册完成 ==="
echo "001机器已成功订阅以下配置："
echo "- common组：app.version, debug.enabled"
echo "- database组：jdbc.url, jdbc.username"
echo "- redis组：host" 