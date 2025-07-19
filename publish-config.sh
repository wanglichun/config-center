#!/bin/bash

echo "=== 手动发布配置到ZooKeeper ==="

# 配置参数
API_BASE="http://localhost:9090/config-center/api"

# 1. 登录获取token
echo "1. 登录获取token..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 登录失败"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ 登录成功，获取到token"
echo ""

# 2. 发布指定配置
echo "2. 发布配置..."

# 发布配置ID 2 (jdbc.username)
echo "发布配置ID 2 (jdbc.username)..."
PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/2/publish" \
  -H "Authorization: Bearer $TOKEN")

if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 2 发布成功"
else
    echo "❌ 配置 2 发布失败"
    echo "响应: $PUBLISH_RESPONSE"
fi
echo ""

# 发布配置ID 4 (redis.host)
echo "发布配置ID 4 (redis.host)..."
PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/4/publish" \
  -H "Authorization: Bearer $TOKEN")

if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 4 发布成功"
else
    echo "❌ 配置 4 发布失败"
    echo "响应: $PUBLISH_RESPONSE"
fi
echo ""

# 发布配置ID 6 (app.version)
echo "发布配置ID 6 (app.version)..."
PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/6/publish" \
  -H "Authorization: Bearer $TOKEN")

if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 6 发布成功"
else
    echo "❌ 配置 6 发布失败"
    echo "响应: $PUBLISH_RESPONSE"
fi
echo ""

# 3. 等待配置同步到ZooKeeper
echo "3. 等待配置同步到ZooKeeper..."
sleep 5

# 4. 检查ZooKeeper中的配置
echo "4. 检查ZooKeeper中的配置..."
ZK_CHECK=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null)

if [ -n "$ZK_CHECK" ]; then
    echo "✅ ZooKeeper中存在配置节点"
    echo "根节点内容: $ZK_CHECK"
    
    # 查看具体的配置路径
    echo ""
    echo "5. 查看具体配置路径..."
    echo "应用配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "数据库配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/database' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "Redis配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/redis' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "通用配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/common' | nc localhost 2181" 2>/dev/null
else
    echo "❌ ZooKeeper中未找到配置节点"
fi

echo ""
echo "=== 配置发布完成 ===" 