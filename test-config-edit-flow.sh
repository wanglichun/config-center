#!/bin/bash

# 测试配置编辑到ticket详情页面的完整流程

echo "=== 测试配置编辑到ticket详情页面的完整流程 ==="

# 1. 启动后端服务
echo "1. 启动后端服务..."
cd /Users/lichun.wang/Git/config-center
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 2. 启动前端服务
echo "2. 启动前端服务..."
cd frontend
npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!

# 等待前端启动
echo "等待前端服务启动..."
sleep 5

# 3. 测试配置编辑API
echo "3. 测试配置编辑API..."
curl -X PUT "http://localhost:8080/api/config/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "groupName": "test-group",
    "configKey": "test-key",
    "dataType": "String",
    "configValue": "new-value",
    "description": "Updated description",
    "encrypted": false
  }' | jq '.'

# 4. 检查返回的ticket信息
echo "4. 检查返回的ticket信息..."
echo "API应该返回包含ticket对象的响应"

# 5. 测试ticket详情API
echo "5. 测试ticket详情API..."
TICKET_ID=$(curl -s -X PUT "http://localhost:8080/api/config/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "groupName": "test-group",
    "configKey": "test-key",
    "dataType": "String",
    "configValue": "new-value",
    "description": "Updated description",
    "encrypted": false
  }' | jq -r '.data.id')

if [ "$TICKET_ID" != "null" ] && [ "$TICKET_ID" != "" ]; then
  echo "获取到的ticket ID: $TICKET_ID"
  
  echo "测试ticket详情API..."
  curl -X GET "http://localhost:8080/api/ticket/$TICKET_ID" \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" | jq '.'
else
  echo "未获取到ticket ID"
fi

# 6. 清理
echo "6. 清理服务..."
kill $BACKEND_PID 2>/dev/null
kill $FRONTEND_PID 2>/dev/null

echo "=== 测试完成 ==="
echo "请检查以下内容："
echo "1. 配置编辑API是否返回ticket对象"
echo "2. 前端是否正确跳转到ticket详情页面"
echo "3. ticket详情页面是否正确显示ticket信息" 