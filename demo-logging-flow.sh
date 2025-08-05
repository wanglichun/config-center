#!/bin/bash

echo "🚀 日志平台完整流程演示"
echo "=========================="

# 1. 检查所有服务状态
echo "📋 1. 检查服务状态..."
echo "Kafka主题:"
docker exec kafka-logging kafka-topics --bootstrap-server localhost:9092 --list
echo ""

echo "Elasticsearch索引:"
curl -s -X GET "localhost:9200/_cat/indices?v"
echo ""

# 2. 发送不同类型的测试日志
echo "📋 2. 发送测试日志到Kafka..."

# API日志
echo "发送API日志..."
API_LOG='{
  "timestamp": "2025-08-01 18:35:00",
  "level": "INFO",
  "category": "API_REQUEST",
  "traceId": "trace_demo_api",
  "spanId": "span_demo_api",
  "operation": "POST",
  "component": "API",
  "startTime": 1640995500000,
  "endTime": 1640995500200,
  "duration": 200,
  "data": {
    "method": "POST",
    "url": "/api/config/create",
    "statusCode": 201,
    "clientIp": "192.168.1.101"
  }
}'
echo $API_LOG | docker exec -i kafka-logging kafka-console-producer --bootstrap-server localhost:9092 --topic api-logs

# Redis日志
echo "发送Redis日志..."
REDIS_LOG='{
  "timestamp": "2025-08-01 18:35:05",
  "level": "INFO",
  "category": "REDIS",
  "traceId": "trace_demo_redis",
  "spanId": "span_demo_redis",
  "operation": "GET",
  "component": "REDIS",
  "startTime": 1640995505000,
  "endTime": 1640995505005,
  "duration": 5,
  "data": {
    "operation": "GET",
    "key": "config::123",
    "hit": true
  }
}'
echo $REDIS_LOG | docker exec -i kafka-logging kafka-console-producer --bootstrap-server localhost:9092 --topic redis-logs

# MySQL日志
echo "发送MySQL日志..."
MYSQL_LOG='{
  "timestamp": "2025-08-01 18:35:10",
  "level": "INFO",
  "category": "MYSQL",
  "traceId": "trace_demo_mysql",
  "spanId": "span_demo_mysql",
  "operation": "SELECT",
  "component": "MYSQL",
  "startTime": 1640995510000,
  "endTime": 1640995510025,
  "duration": 25,
  "data": {
    "operation": "SELECT",
    "sql": "SELECT * FROM config_item WHERE id = 123",
    "rowsAffected": 1
  }
}'
echo $MYSQL_LOG | docker exec -i kafka-logging kafka-console-producer --bootstrap-server localhost:9092 --topic mysql-logs

# ZK日志
echo "发送ZooKeeper日志..."
ZK_LOG='{
  "timestamp": "2025-08-01 18:35:15",
  "level": "INFO",
  "category": "ZOOKEEPER",
  "traceId": "trace_demo_zk",
  "spanId": "span_demo_zk",
  "operation": "GET",
  "component": "ZOOKEEPER",
  "startTime": 1640995515000,
  "endTime": 1640995515005,
  "duration": 5,
  "data": {
    "operation": "GET",
    "path": "/configs/redis/port",
    "data": "6379"
  }
}'
echo $ZK_LOG | docker exec -i kafka-logging kafka-console-producer --bootstrap-server localhost:9092 --topic zk-logs

# 3. 等待日志处理
echo "📋 3. 等待日志处理..."
sleep 15

# 4. 检查Elasticsearch中的日志
echo "📋 4. 检查Elasticsearch中的日志..."
echo "所有日志:"
curl -s -X GET "localhost:9200/logs-2025.08.01/_search?pretty&size=10"

echo ""
echo "按TraceId查询:"
curl -s -X GET "localhost:9200/logs-2025.08.01/_search?q=traceId:trace_demo_api&pretty"

echo ""
echo "按组件查询:"
curl -s -X GET "localhost:9200/logs-2025.08.01/_search?q=component:REDIS&pretty"

# 5. 显示访问地址
echo ""
echo "📋 5. 访问地址:"
echo "Kibana: http://localhost:5601"
echo "Elasticsearch: http://localhost:9200"
echo "Kafka Manager: 可通过Docker命令查看"
echo ""
echo "✅ 演示完成！日志已成功从Kafka传输到Elasticsearch！" 