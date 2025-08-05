#!/bin/bash

echo "🔍 检查Kibana时区设置..."

# 检查Kibana状态
echo "📊 Kibana状态:"
curl -s "localhost:5601/api/status" | jq '.status.overall.level' 2>/dev/null || echo "Kibana未响应"

# 检查Elasticsearch中的日志时区
echo ""
echo "📅 检查最新日志的时区信息:"
curl -s "localhost:9200/logs-*/_search?pretty&size=3" | jq '.hits.hits[] | {timestamp: ._source.timestamp, @timestamp: ._source["@timestamp"]}' 2>/dev/null || echo "无法获取日志数据"

echo ""
echo "🌍 建议的解决方案:"
echo "1. 在Kibana中设置时区为UTC:"
echo "   - 进入Kibana -> Management -> Advanced Settings"
echo "   - 搜索 'timezone'"
echo "   - 设置为 'UTC'"
echo ""
echo "2. 或者在Kibana的Discover页面:"
echo "   - 点击右上角的时区设置"
echo "   - 选择 'UTC'"
echo ""
echo "3. 重新加载Kibana页面以应用设置" 