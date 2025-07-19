#!/bin/bash

echo "=== 发布现有配置到ZooKeeper ==="

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

# 2. 查询所有配置
echo "2. 查询所有配置..."
CONFIG_RESPONSE=$(curl -s -X GET "$API_BASE/config/page?pageNum=1&pageSize=50" \
  -H "Authorization: Bearer $TOKEN")

# 提取未发布的配置ID
UNPUBLISHED_IDS=$(echo "$CONFIG_RESPONSE" | grep -o '"id":[0-9]*,"status":"ACTIVE"' | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -z "$UNPUBLISHED_IDS" ]; then
    echo "✅ 所有配置都已发布"
    exit 0
fi

echo "发现未发布的配置ID: $UNPUBLISHED_IDS"
echo ""

# 3. 发布每个未发布的配置
echo "3. 开始发布配置..."
for CONFIG_ID in $UNPUBLISHED_IDS; do
    echo "发布配置ID: $CONFIG_ID"
    
    PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/$CONFIG_ID/publish" \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
        echo "✅ 配置 $CONFIG_ID 发布成功"
    else
        echo "❌ 配置 $CONFIG_ID 发布失败"
        echo "响应: $PUBLISH_RESPONSE"
    fi
    echo ""
done

# 4. 等待配置同步到ZooKeeper
echo "4. 等待配置同步到ZooKeeper..."
sleep 5

# 5. 检查ZooKeeper中的配置
echo "5. 检查ZooKeeper中的配置..."
ZK_CHECK=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null)

if [ -n "$ZK_CHECK" ]; then
    echo "✅ ZooKeeper中存在配置节点"
    echo "节点内容: $ZK_CHECK"
    
    # 查看具体的配置路径
    echo ""
    echo "6. 查看具体配置路径..."
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev' | nc localhost 2181" 2>/dev/null
    echo ""
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/database' | nc localhost 2181" 2>/dev/null
    echo ""
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/redis' | nc localhost 2181" 2>/dev/null
    echo ""
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/common' | nc localhost 2181" 2>/dev/null
else
    echo "❌ ZooKeeper中未找到配置节点"
fi

echo ""
echo "=== 配置发布完成 ===" 