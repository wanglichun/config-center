#!/bin/bash

echo "🎯 Kibana自动设置脚本"
echo "====================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 检查Kibana是否可访问
echo -e "${YELLOW}检查Kibana连接...${NC}"
if ! curl -s "localhost:5601" > /dev/null; then
    echo -e "${RED}错误: 无法连接到Kibana，请确保服务正在运行${NC}"
    exit 1
fi

echo -e "${GREEN}Kibana连接正常${NC}"

# 等待Kibana完全启动
echo -e "${YELLOW}等待Kibana完全启动...${NC}"
sleep 10

# 创建索引模式
echo -e "${YELLOW}创建索引模式...${NC}"
response=$(curl -s -X POST "localhost:5601/api/saved_objects/index-pattern/logs-pattern" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -d '{
    "attributes": {
      "title": "logs-*",
      "timeFieldName": "@timestamp"
    }
  }')

if echo "$response" | grep -q "already exists"; then
    echo -e "${GREEN}索引模式已存在${NC}"
elif echo "$response" | grep -q "id"; then
    echo -e "${GREEN}索引模式创建成功${NC}"
else
    echo -e "${RED}索引模式创建失败: $response${NC}"
fi

# 检查Elasticsearch中的日志数据
echo -e "${YELLOW}检查日志数据...${NC}"
log_count=$(curl -s -X GET "localhost:9200/logs-*/_count" | jq -r '.count')
echo -e "${GREEN}当前有 $log_count 条日志记录${NC}"

# 显示访问信息
echo ""
echo -e "${GREEN}🎉 Kibana设置完成！${NC}"
echo ""
echo "📋 访问信息:"
echo "  Kibana地址: http://localhost:5601"
echo "  索引模式: logs-*"
echo "  日志数量: $log_count 条"
echo ""
echo "🔍 快速查询示例:"
echo "  查看所有日志: 在Discover中选择 logs-* 索引模式"
echo "  按TraceId查询: traceId:trace_demo_api"
echo "  按组件查询: category:REDIS"
echo "  按状态码查询: status_code:200"
echo "  查看慢查询: duration:>100"
echo ""
echo "📖 详细使用指南请查看: kibana-setup.md" 