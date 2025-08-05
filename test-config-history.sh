#!/bin/bash

echo "🧪 测试配置历史功能..."

# 获取配置ID (假设为10)
CONFIG_ID=10

echo "📋 测试获取配置历史..."
echo "配置ID: $CONFIG_ID"

# 调用配置历史接口
echo ""
echo "🔍 调用配置历史接口:"
curl -s "localhost:9090/config-center/config/history?configId=$CONFIG_ID&pageNum=1&pageSize=10" | jq '.'

echo ""
echo "✅ 测试完成！"
echo ""
echo "📊 预期返回数据结构:"
echo "{
  \"code\": 0,
  \"message\": \"操作成功\",
  \"data\": {
    \"records\": [
      {
        \"id\": 工单ID,
        \"title\": \"工单标题\",
        \"applicator\": \"申请人\",
        \"phase\": \"工单状态\",
        \"createTime\": 创建时间,
        \"oldData\": \"变更前数据\",
        \"newData\": \"变更后数据\"
      }
    ],
    \"total\": 总数,
    \"pageNum\": 当前页,
    \"pageSize\": 页大小
  }
}" 