#!/bin/bash

echo "=== 配置发布流程测试 ==="

# 配置参数
API_BASE="http://localhost:9090/config-center/api"
APP_NAME="demo-app"
ENV="dev"
GROUP_NAME="redis"
CONFIG_KEY="host"
CONFIG_VALUE="redis-server-01"

echo "配置参数:"
echo "  应用名称: $APP_NAME"
echo "  环境: $ENV"
echo "  配置组: $GROUP_NAME"
echo "  配置键: $CONFIG_KEY"
echo "  配置值: $CONFIG_VALUE"
echo ""

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

# 2. 创建配置
echo "2. 创建配置..."
CREATE_RESPONSE=$(curl -s -X POST "$API_BASE/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"appName\": \"$APP_NAME\",
    \"environment\": \"$ENV\",
    \"groupName\": \"$GROUP_NAME\",
    \"configKey\": \"$CONFIG_KEY\",
    \"configValue\": \"$CONFIG_VALUE\",
    \"dataType\": \"STRING\",
    \"description\": \"Redis主机地址\"
  }")

if echo "$CREATE_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 配置创建成功"
    CONFIG_ID=$(echo $CREATE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "配置ID: $CONFIG_ID"
else
    echo "❌ 配置创建失败"
    echo "响应: $CREATE_RESPONSE"
    exit 1
fi
echo ""

# 3. 发布配置
echo "3. 发布配置..."
if [ -n "$CONFIG_ID" ]; then
    PUBLISH_RESPONSE=$(curl -s -X POST "$API_BASE/config/$CONFIG_ID/publish" \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$PUBLISH_RESPONSE" | grep -q '"success":true'; then
        echo "✅ 配置发布成功"
    else
        echo "❌ 配置发布失败"
        echo "响应: $PUBLISH_RESPONSE"
        exit 1
    fi
else
    echo "❌ 未找到配置ID"
    exit 1
fi
echo ""

# 4. 检查ZooKeeper中的配置
echo "4. 检查ZooKeeper中的配置..."
ZK_PATH="/config-center/configs/$APP_NAME/$ENV/$GROUP_NAME/$CONFIG_KEY"

echo "ZooKeeper路径: $ZK_PATH"

# 等待一下让配置同步到ZooKeeper
sleep 3

# 检查ZooKeeper节点是否存在
ZK_CHECK=$(docker exec config-center-zookeeper sh -c "echo 'ls /config-center/configs' | nc localhost 2181" 2>/dev/null)

if [ -n "$ZK_CHECK" ]; then
    echo "✅ ZooKeeper中存在配置节点"
    echo "节点内容: $ZK_CHECK"
else
    echo "❌ ZooKeeper中未找到配置节点"
fi

# 5. 验证配置值
echo ""
echo "5. 验证配置值..."
CONFIG_RESPONSE=$(curl -s -X GET "$API_BASE/config/page?appName=$APP_NAME&environment=$ENV&pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN")

echo "配置查询结果:"
echo "$CONFIG_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CONFIG_RESPONSE"

echo ""
echo "=== 配置发布流程完成 ==="
echo "如果ZooKeeper中看到配置节点，说明配置已成功推送到ZooKeeper" 