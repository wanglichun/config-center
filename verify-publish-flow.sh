#!/bin/bash

echo "=== 配置发布流程验证 ==="

# 1. 登录获取JWT token
echo "1. 登录获取JWT token..."
TOKEN=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ 登录失败"
    exit 1
fi
echo "✅ 登录成功，获取到JWT token"

# 2. 查询配置列表
echo "2. 查询配置列表..."
CONFIG_RESPONSE=$(curl -s -X GET "http://localhost:9090/config-center/api/config/page?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN")

echo "配置列表响应:"
echo "$CONFIG_RESPONSE" | jq '.'

# 3. 发布配置ID=5
echo "3. 发布配置ID=5..."
PUBLISH_RESPONSE=$(curl -s -X POST "http://localhost:9090/config-center/api/config/5/publish" \
  -H "Authorization: Bearer $TOKEN")

echo "发布响应:"
echo "$PUBLISH_RESPONSE" | jq '.'

# 4. 验证ZooKeeper中的配置
echo "4. 验证ZooKeeper中的配置..."
echo "检查ZooKeeper连接状态..."
echo "ruok" | nc localhost 2181

echo "检查配置节点是否存在..."
echo "ls /config-center" | nc localhost 2181

echo "获取配置值..."
echo "get /config-center/configs/demo-app/dev/redis/port" | nc localhost 2181

# 5. 检查后端日志
echo "5. 检查后端服务日志..."
echo "最近20行日志:"
docker logs config-center-backend --tail 20

echo "=== 验证完成 ===" 