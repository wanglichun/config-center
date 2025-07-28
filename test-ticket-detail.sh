#!/bin/bash

echo "测试ticket详情API..."

# 获取ticket详情（假设ID为1）
echo "1. 获取ticket详情..."
curl -X GET http://localhost:8080/api/ticket/1

echo -e "\n\n2. 分页查询ticket列表..."
curl -X GET "http://localhost:8080/api/ticket/page?pageNum=1&pageSize=10"

echo -e "\n\n测试完成" 