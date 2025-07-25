#!/bin/bash

echo "测试创建ticket API..."

# 创建ticket
echo "1. 创建ticket..."
curl -X POST http://localhost:8080/api/ticket \
  -H "Content-Type: application/json" \
  -d '{
    "dataId": 1,
    "title": "测试工单",
    "newData": "新的配置值"
  }'

echo -e "\n\n2. 获取ticket详情..."
# 获取ticket详情（假设ID为1）
curl -X GET http://localhost:8080/api/ticket/1

echo -e "\n\n测试完成" 