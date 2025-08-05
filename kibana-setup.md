# 🎯 Kibana日志查看设置指南

## 📋 快速设置步骤

### 1. 访问Kibana
```
http://localhost:5601
```

### 2. 创建索引模式 (Index Pattern)

#### 方法1: 通过UI界面
1. 点击左侧菜单 **"Stack Management"**
2. 选择 **"Index Patterns"**
3. 点击 **"Create index pattern"**
4. 输入: `logs-*`
5. 点击 **"Next step"**
6. 选择时间字段: `@timestamp`
7. 点击 **"Create index pattern"**

#### 方法2: 通过API (推荐)
```bash
# 创建索引模式
curl -X POST "localhost:5601/api/saved_objects/index-pattern/logs-pattern" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -d '{
    "attributes": {
      "title": "logs-*",
      "timeFieldName": "@timestamp"
    }
  }'
```

### 3. 使用Discover查看日志

#### 基本操作
1. 点击左侧菜单 **"Discover"**
2. 选择索引模式: `logs-*`
3. 设置时间范围
4. 点击 **"Refresh"**

#### 高级查询
在搜索框中输入以下查询：

**按TraceId查询:**
```
traceId:trace_demo_api
```

**按组件类型查询:**
```
category:REDIS
category:MYSQL
category:API_REQUEST
category:ZOOKEEPER
```

**按状态码查询:**
```
status_code:200
status_code:201
```

**按响应时间查询:**
```
duration:>100
```

**组合查询:**
```
category:API_REQUEST AND status_code:200
traceId:trace_demo_api AND duration:>150
```

### 4. 创建可视化图表

#### 4.1 创建柱状图
1. 点击 **"Visualize Library"**
2. 点击 **"Create visualization"**
3. 选择 **"Bar"**
4. 选择索引模式: `logs-*`
5. 配置:
   - X轴: `category.keyword`
   - Y轴: `Count`

#### 4.2 创建时间序列图
1. 选择 **"Line"**
2. 配置:
   - X轴: `@timestamp`
   - Y轴: `Count`
   - Split Series: `category.keyword`

#### 4.3 创建饼图
1. 选择 **"Pie"**
2. 配置:
   - Slice: `category.keyword`
   - Size: `Count`

### 5. 创建Dashboard

1. 点击 **"Dashboard"**
2. 点击 **"Create dashboard"**
3. 添加已创建的可视化图表
4. 保存Dashboard

### 6. 常用查询示例

#### 6.1 查看所有API请求
```
category:API_REQUEST
```

#### 6.2 查看慢查询
```
duration:>1000
```

#### 6.3 查看错误日志
```
level:ERROR
```

#### 6.4 查看特定用户的请求
```
client_ip:192.168.1.101
```

#### 6.5 查看特定时间段的日志
```
@timestamp:[2025-08-01T18:30:00 TO 2025-08-01T18:40:00]
```

### 7. 字段说明

| 字段名 | 说明 | 示例值 |
|--------|------|--------|
| `traceId` | 链路追踪ID | `trace_demo_api` |
| `category` | 日志分类 | `API_REQUEST`, `REDIS`, `MYSQL`, `ZOOKEEPER` |
| `operation` | 操作类型 | `GET`, `POST`, `SELECT`, `SET` |
| `duration` | 响应时间(毫秒) | `150`, `200` |
| `status_code` | HTTP状态码 | `200`, `201`, `500` |
| `client_ip` | 客户端IP | `192.168.1.101` |
| `level` | 日志级别 | `INFO`, `ERROR`, `WARN` |
| `@timestamp` | 时间戳 | `2025-08-01T18:35:00.000Z` |

### 8. 实用技巧

#### 8.1 保存搜索
1. 在Discover中输入查询条件
2. 点击 **"Save"**
3. 输入名称和描述
4. 点击 **"Confirm Save"**

#### 8.2 创建告警
1. 在Stack Management中选择 **"Rules and Alerts"**
2. 创建新的告警规则
3. 设置触发条件

#### 8.3 导出数据
1. 在Discover中设置查询条件
2. 点击 **"Share"**
3. 选择导出格式 (CSV, JSON)

### 9. 故障排查

#### 9.1 Kibana无法访问
```bash
# 检查Kibana容器状态
docker ps | grep kibana

# 查看Kibana日志
docker logs kibana-logging
```

#### 9.2 没有日志数据
```bash
# 检查Elasticsearch索引
curl -X GET "localhost:9200/_cat/indices?v"

# 检查索引中的文档
curl -X GET "localhost:9200/logs-*/_count"
```

#### 9.3 索引模式创建失败
```bash
# 检查索引是否存在
curl -X GET "localhost:9200/logs-*/_mapping"
```

### 10. 性能优化

#### 10.1 索引优化
- 定期删除旧索引
- 设置合适的分片数
- 配置索引生命周期管理

#### 10.2 查询优化
- 使用具体的时间范围
- 避免使用通配符查询
- 合理使用聚合查询

---

**🎉 现在您可以通过Kibana方便地查看和分析日志了！** 