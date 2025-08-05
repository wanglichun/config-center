# 🚀 基于Kafka + Elasticsearch的日志平台使用指南

## 📋 系统架构

```
应用层 → Kafka → Logstash → Elasticsearch → Kibana
```

### 组件说明

- **Kafka**: 消息队列，用于接收和缓冲日志数据
- **Logstash**: 数据处理管道，从Kafka消费日志并发送到Elasticsearch
- **Elasticsearch**: 搜索引擎，存储和索引日志数据
- **Kibana**: 可视化界面，用于日志检索和分析

## 🛠️ 快速启动

### 1. 启动日志平台
```bash
docker-compose -f docker-compose-logging.yml up -d
```

### 2. 检查服务状态
```bash
# 检查容器状态
docker ps

# 检查Kafka主题
docker exec kafka-logging kafka-topics --bootstrap-server localhost:9092 --list

# 检查Elasticsearch
curl -X GET "localhost:9200/_cluster/health?pretty"
```

## 📊 日志流程演示

### 运行演示脚本
```bash
./demo-logging-flow.sh
```

这个脚本会：
1. 检查所有服务状态
2. 发送不同类型的测试日志到Kafka
3. 等待Logstash处理
4. 验证日志是否到达Elasticsearch
5. 演示日志检索功能

## 🔍 日志检索

### 1. 通过API检索
```bash
# 按TraceId检索
curl -X GET "localhost:9200/logs-*/_search?q=traceId:trace_demo_api&pretty"

# 按组件检索
curl -X GET "localhost:9200/logs-*/_search?q=component:REDIS&pretty"

# 按时间范围检索
curl -X GET "localhost:9200/logs-*/_search?q=@timestamp:[2025-08-01T18:30:00 TO 2025-08-01T18:40:00]&pretty"
```

### 2. 通过Kibana检索
访问 http://localhost:5601 进行可视化检索

## 📝 日志格式

### 统一日志格式
```json
{
  "timestamp": "2025-08-01 18:30:00",
  "level": "INFO",
  "category": "API_REQUEST|REDIS|MYSQL|ZOOKEEPER",
  "traceId": "trace_abc123",
  "spanId": "span_abc123",
  "parentSpanId": "span_parent123",
  "operation": "GET|POST|SELECT|SET",
  "component": "API|REDIS|MYSQL|ZOOKEEPER",
  "startTime": 1640995200000,
  "endTime": 1640995200150,
  "duration": 150,
  "data": {
    // 具体业务数据
  },
  "tags": {
    // 标签信息
  }
}
```

## 🔧 配置说明

### 1. Kafka配置
- **端口**: 9092
- **主题**: api-logs, redis-logs, mysql-logs, zk-logs, trace-logs
- **分区数**: 3
- **副本数**: 1

### 2. Logstash配置
- **输入**: 从Kafka消费JSON格式日志
- **过滤**: 解析时间戳、TraceId、组件类型等
- **输出**: 发送到Elasticsearch，按日期分索引

### 3. Elasticsearch配置
- **端口**: 9200
- **索引模式**: logs-YYYY.MM.DD
- **分片数**: 1
- **副本数**: 1

## 🎯 使用场景

### 1. 全链路追踪
通过TraceId关联一次请求中的所有组件调用：
- API请求
- Redis操作
- MySQL查询
- ZooKeeper操作

### 2. 性能监控
- 响应时间统计
- 慢查询分析
- 错误率监控

### 3. 业务分析
- 接口调用统计
- 用户行为分析
- 系统健康度监控

## 🚨 故障排查

### 1. Kafka问题
```bash
# 检查Kafka状态
docker logs kafka-logging

# 检查主题
docker exec kafka-logging kafka-topics --bootstrap-server localhost:9092 --list
```

### 2. Logstash问题
```bash
# 检查Logstash状态
docker logs logstash-logging

# 检查Logstash管道
docker exec logstash-logging curl -s http://localhost:9600/_node/stats/pipeline
```

### 3. Elasticsearch问题
```bash
# 检查Elasticsearch状态
curl -X GET "localhost:9200/_cluster/health?pretty"

# 检查索引
curl -X GET "localhost:9200/_cat/indices?v"
```

## 📈 监控指标

### 1. 吞吐量
- 每秒日志处理量
- 各组件调用频率

### 2. 延迟
- 日志处理延迟
- 各组件响应时间

### 3. 错误率
- 日志处理错误率
- 各组件错误率

## 🔄 扩展功能

### 1. 添加新的日志类型
1. 在Kafka中创建新主题
2. 在Logstash配置中添加过滤规则
3. 在应用中发送对应格式的日志

### 2. 自定义索引模板
```json
{
  "index_patterns": ["logs-*"],
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "traceId": {"type": "keyword"},
      "category": {"type": "keyword"},
      "duration": {"type": "long"}
    }
  }
}
```

### 3. 告警配置
- 慢查询告警
- 错误率告警
- 系统资源告警

## 🎉 总结

这个日志平台提供了：
- ✅ 高吞吐量的日志收集
- ✅ 实时日志处理
- ✅ 强大的检索能力
- ✅ 可视化监控界面
- ✅ 全链路追踪支持

通过Kafka + Elasticsearch的组合，我们实现了企业级的日志管理解决方案！ 