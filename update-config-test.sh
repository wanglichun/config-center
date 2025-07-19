#!/bin/bash

echo "=== 更新已发布配置到ZooKeeper ==="

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

# 2. 查询已发布的配置
echo "2. 查询已发布的配置..."
CONFIG_RESPONSE=$(curl -s -X GET "$API_BASE/config/page?pageNum=1&pageSize=50" \
  -H "Authorization: Bearer $TOKEN")

# 提取已发布的配置ID
PUBLISHED_IDS=$(echo "$CONFIG_RESPONSE" | grep -o '"id":[0-9]*,"status":"PUBLISHED"' | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -3)

if [ -z "$PUBLISHED_IDS" ]; then
    echo "❌ 没有找到已发布的配置"
    exit 1
fi

echo "发现已发布的配置ID: $PUBLISHED_IDS"
echo ""

# 3. 更新第一个已发布的配置
FIRST_ID=$(echo $PUBLISHED_IDS | cut -d' ' -f1)
NEW_VALUE="updated-value-$(date +%s)"

echo "3. 更新配置ID: $FIRST_ID，新值: $NEW_VALUE"

UPDATE_RESPONSE=$(curl -s -X PUT "$API_BASE/config/$FIRST_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"configValue\": \"$NEW_VALUE\",
    \"description\": \"更新测试 - $(date)\"
  }")

if echo "$UPDATE_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 $FIRST_ID 更新成功"
else
    echo "❌ 配置 $FIRST_ID 更新失败"
    echo "响应: $UPDATE_RESPONSE"
    exit 1
fi
echo ""

# 4. 重新发布配置
echo "4. 重新发布配置..."
PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/$FIRST_ID/publish" \
  -H "Authorization: Bearer $TOKEN")

if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置 $FIRST_ID 重新发布成功"
else
    echo "❌ 配置 $FIRST_ID 重新发布失败"
    echo "响应: $PUBLISH_RESPONSE"
    exit 1
fi
echo ""

# 5. 等待配置同步到ZooKeeper
echo "5. 等待配置同步到ZooKeeper..."
sleep 5

# 6. 检查ZooKeeper中的配置
echo "6. 检查ZooKeeper中的配置..."
ZK_CHECK=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null)

if [ -n "$ZK_CHECK" ]; then
    echo "✅ ZooKeeper中存在配置节点"
    echo "根节点内容: $ZK_CHECK"
    
    # 查看具体的配置路径
    echo ""
    echo "7. 查看具体配置路径..."
    echo "demo-app配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "dev环境配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "database配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/database' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "redis配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/redis' | nc localhost 2181" 2>/dev/null
    echo ""
    echo "common配置路径:"
    docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs/demo-app/dev/common' | nc localhost 2181" 2>/dev/null
else
    echo "❌ ZooKeeper中未找到配置节点"
fi

# 7. 验证配置状态
echo ""
echo "8. 验证配置状态..."
CONFIG_RESPONSE=$(curl -s -X GET "$API_BASE/config/page?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN")

echo "最新配置状态:"
echo "$CONFIG_RESPONSE" | python3 -m json.tool 2>/dev/null | grep -A 5 -B 5 '"id":'$FIRST_ID || echo "$CONFIG_RESPONSE"

echo ""
echo "=== 配置更新测试完成 ===" 