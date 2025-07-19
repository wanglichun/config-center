#!/bin/bash

echo "=== 发布ACTIVE配置到ZooKeeper ==="

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

# 2. 发布配置ID 3 (jdbc.password)
echo "2. 发布配置ID 3 (jdbc.password)..."
PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/3/publish" \
  -H "Authorization: Bearer $TOKEN")

if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 3 发布成功"
else
    echo "❌ 配置 3 发布失败"
    echo "响应: $PUBLISH_RESPONSE"
    exit 1
fi
echo ""

# 3. 等待配置同步到ZooKeeper
echo "3. 等待配置同步到ZooKeeper..."
sleep 5

# 4. 检查ZooKeeper中的配置 (使用正确的路径)
echo "4. 检查ZooKeeper中的配置..."
echo "检查路径: /configs/demo-app/dev/database/jdbc.password"

# 检查ZooKeeper节点是否存在
ZK_CHECK=$(docker exec config-center-zookeeper sh -c "echo 'ls /configs' | nc localhost 2181" 2>/dev/null)

if [ -n "$ZK_CHECK" ]; then
    echo "✅ ZooKeeper中存在configs节点"
    echo "configs节点内容: $ZK_CHECK"
    
    # 查看具体的配置路径
    echo ""
    echo "5. 查看具体配置路径..."
    echo "demo-app配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /configs/demo-app' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "dev环境配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /configs/demo-app/dev' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "database配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /configs/demo-app/dev/database' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "jdbc.password配置值:"
    docker exec config-center-zookeeper sh -c "echo 'get /configs/demo-app/dev/database/jdbc.password' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "redis配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /configs/demo-app/dev/redis' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "common配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /configs/demo-app/dev/common' | nc localhost 2181" 2>/dev/null
else
    echo "❌ ZooKeeper中未找到configs节点"
    
    # 尝试检查其他可能的路径
    echo ""
    echo "尝试检查其他路径..."
    echo "检查 /config-center/configs:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "检查根节点:"
    docker exec config-center-zookeeper sh -c "echo 'ls /' | nc localhost 2181" 2>/dev/null
fi

# 6. 验证配置状态
echo ""
echo "6. 验证配置状态..."
CONFIG_RESPONSE=$(curl -s -X GET "$API_BASE/config/page?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN")

echo "配置ID 3的状态:"
echo "$CONFIG_RESPONSE" | python3 -m json.tool 2>/dev/null | grep -A 10 -B 5 '"id": 3' || echo "$CONFIG_RESPONSE"

echo ""
echo "=== 配置发布测试完成 ===" 