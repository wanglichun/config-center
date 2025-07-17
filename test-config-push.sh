#!/bin/bash

# 配置推送测试脚本
echo "🚀 开始测试配置推送功能..."

# 等待后端服务启动
echo "⏳ 等待后端服务启动..."
sleep 5

# 获取认证token
echo "🔐 获取认证token..."
TOKEN=$(curl -s -X POST "http://localhost:9090/config-center/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，请检查用户名密码"
    exit 1
fi

echo "✅ 登录成功，token: ${TOKEN:0:20}..."

# 测试ZooKeeper连接
echo "🔗 测试ZooKeeper连接..."
docker exec zk1 echo srvr | nc localhost 2181 | head -5

# 创建测试配置
echo "📝 创建测试配置..."
curl -X POST "http://localhost:9090/config-center/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "prod",
    "groupName": "database",
    "configKey": "db.url",
    "configValue": "jdbc:mysql://localhost:3306/test",
    "dataType": "STRING",
    "description": "数据库连接URL",
    "encrypted": false
  }' | jq .

echo ""
echo "📝 创建第二个测试配置..."
curl -X POST "http://localhost:9090/config-center/api/config" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "prod",
    "groupName": "database",
    "configKey": "db.maxPoolSize",
    "configValue": "20",
    "dataType": "NUMBER",
    "description": "数据库连接池最大连接数",
    "encrypted": false
  }' | jq .

echo ""
echo "📋 获取配置列表..."
CONFIG_RESPONSE=$(curl -s -X GET "http://localhost:9090/config-center/api/config?appName=demo-app&environment=prod&page=1&size=10" \
  -H "Authorization: Bearer $TOKEN")
echo "$CONFIG_RESPONSE" | jq .

# 提取配置ID
CONFIG_ID=$(echo "$CONFIG_RESPONSE" | jq -r '.data.records[0].id')
echo "📋 配置ID: $CONFIG_ID"

echo ""
echo "🔍 检查ZooKeeper中的配置节点..."
docker exec zk1 /usr/local/bin/zkCli.sh -server localhost:2181 ls /config-center/demo-app/prod/database 2>/dev/null || echo "节点不存在"

echo ""
echo "📤 发布配置到ZooKeeper..."
curl -X POST "http://localhost:9090/config-center/api/config/$CONFIG_ID/publish" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"publisher": "admin"}' | jq .

echo ""
echo "🔍 验证ZooKeeper中的配置值..."
docker exec zk1 /usr/local/bin/zkCli.sh -server localhost:2181 get /config-center/demo-app/prod/database/db.url 2>/dev/null || echo "配置节点不存在"

echo ""
echo "🔄 更新配置值..."
curl -X PUT "http://localhost:9090/config-center/api/config/$CONFIG_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "appName": "demo-app",
    "environment": "prod",
    "groupName": "database",
    "configKey": "db.url",
    "configValue": "jdbc:mysql://localhost:3306/prod",
    "dataType": "STRING",
    "description": "数据库连接URL（更新）",
    "encrypted": false
  }' | jq .

echo ""
echo "📤 重新发布更新后的配置..."
curl -X POST "http://localhost:9090/config-center/api/config/$CONFIG_ID/publish" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"publisher": "admin"}' | jq .

echo ""
echo "🔍 验证更新后的配置值..."
docker exec zk1 /usr/local/bin/zkCli.sh -server localhost:2181 get /config-center/demo-app/prod/database/db.url 2>/dev/null || echo "配置节点不存在"

echo ""
echo "📋 查看配置历史..."
curl -X GET "http://localhost:9090/config-center/api/config/$CONFIG_ID/history" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo ""
echo "🔍 检查ZooKeeper通知节点..."
docker exec zk1 /usr/local/bin/zkCli.sh -server localhost:2181 ls /config-center/demo-app/prod/notify 2>/dev/null || echo "通知节点不存在"

echo ""
echo "✅ 配置推送测试完成！" 