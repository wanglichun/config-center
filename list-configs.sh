#!/bin/bash

echo "=== 查看现有配置 ==="

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

echo "配置列表:"
echo "$CONFIG_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CONFIG_RESPONSE"

echo ""
echo "=== 配置列表查询完成 ===" 