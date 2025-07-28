#!/bin/bash

echo "=== 测试ticket详情页面访问 ==="

# 1. 检查前端服务是否运行
echo "1. 检查前端服务..."
if curl -s http://localhost:3009 > /dev/null 2>&1; then
  echo "✅ 前端服务运行正常 (端口: 3009)"
else
  echo "❌ 前端服务未运行"
  exit 1
fi

# 2. 检查后端服务是否运行
echo "2. 检查后端服务..."
if curl -s http://localhost:8080/api/config/page > /dev/null 2>&1; then
  echo "✅ 后端服务运行正常 (端口: 8080)"
else
  echo "❌ 后端服务未运行"
  echo "请先启动后端服务: mvn spring-boot:run"
  exit 1
fi

# 3. 测试ticket详情API
echo "3. 测试ticket详情API..."
# 先创建一个测试ticket
echo "创建测试ticket..."
TICKET_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/ticket" \
  -H "Content-Type: application/json" \
  -d '{
    "dataId": 1,
    "title": "测试工单",
    "newData": "test-value"
  }')

echo "Ticket创建响应: $TICKET_RESPONSE"

# 提取ticket ID
TICKET_ID=$(echo $TICKET_RESPONSE | grep -o '"data":[0-9]*' | cut -d':' -f2)

if [ -n "$TICKET_ID" ] && [ "$TICKET_ID" != "null" ]; then
  echo "✅ 获取到ticket ID: $TICKET_ID"
  
  # 测试ticket详情API
  echo "测试ticket详情API..."
  TICKET_DETAIL=$(curl -s -X GET "http://localhost:8080/api/ticket/$TICKET_ID")
  echo "Ticket详情响应: $TICKET_DETAIL"
  
  if echo "$TICKET_DETAIL" | grep -q '"success":true'; then
    echo "✅ Ticket详情API正常"
  else
    echo "❌ Ticket详情API异常"
  fi
else
  echo "❌ 未获取到ticket ID"
fi

# 4. 测试前端路由
echo "4. 测试前端路由..."
echo "请在浏览器中访问以下URL测试ticket详情页面:"
echo "http://localhost:3009/ticket/detail/$TICKET_ID"

# 5. 测试配置编辑流程
echo "5. 测试配置编辑流程..."
echo "请按以下步骤测试:"
echo "1. 访问 http://localhost:3009/config"
echo "2. 点击某个配置的'详情'按钮"
echo "3. 在详情页面点击'编辑'按钮"
echo "4. 修改配置值并提交"
echo "5. 检查是否自动跳转到ticket详情页面"

echo "=== 测试完成 ===" 