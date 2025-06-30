# 灰度发布功能说明

## 概述

灰度发布是一种安全的配置发布策略，可以将配置变更逐步推送到部分实例，验证无误后再全量发布。这种方式可以有效降低配置变更带来的风险。

## 核心特性

### 1. 多种灰度策略

- **IP白名单**: 指定特定IP地址的实例获取灰度配置
- **用户白名单**: 指定特定用户获取灰度配置
- **按比例发布**: 按照设定的百分比随机选择实例
- **金丝雀发布**: 先发布到指定的金丝雀实例，然后逐步扩大范围

### 2. 完整的发布流程

1. **创建发布计划**: 选择要发布的配置、策略和规则
2. **开始灰度**: 启动灰度发布，部分实例开始获取新配置
3. **监控验证**: 观察灰度实例的运行状况
4. **全量发布**: 验证无误后，将配置推送到所有实例
5. **应急回滚**: 发现问题时，可以快速回滚到原配置

### 3. 安全保障机制

- **自动回滚**: 支持基于监控指标的自动回滚
- **操作审计**: 记录所有发布操作的详细日志
- **权限控制**: 严格的权限管理，确保操作安全
- **实时监控**: 实时监控发布过程中的各项指标

## 数据库设计

### 核心表结构

1. **gray_release_plan**: 灰度发布计划表
   - 存储发布计划的基本信息、策略、规则等
   
2. **gray_release_detail**: 灰度发布详情表
   - 存储每个配置项的灰度发布状态
   
3. **gray_release_log**: 灰度发布日志表
   - 记录所有操作日志，便于审计和问题排查
   
4. **gray_release_monitor**: 灰度发布监控表
   - 存储监控指标数据，支持自动回滚决策
   
5. **gray_release_instance**: 灰度发布实例表
   - 记录哪些实例获取了灰度配置

## 使用场景

### 1. 数据库配置变更

```json
{
  "planName": "数据库连接池配置优化",
  "appName": "user-service",
  "environment": "prod",
  "configKeys": ["database.maxPoolSize", "database.connectionTimeout"],
  "grayStrategy": "IP_WHITELIST",
  "grayRules": {
    "ipWhitelist": ["192.168.1.100", "192.168.1.101"]
  }
}
```

### 2. 功能开关灰度

```json
{
  "planName": "新功能开关灰度",
  "appName": "order-service",
  "environment": "prod",
  "configKeys": ["feature.newPayment.enabled"],
  "grayStrategy": "PERCENTAGE",
  "grayRules": {
    "percentage": 10
  }
}
```

### 3. 金丝雀发布

```json
{
  "planName": "缓存配置金丝雀发布",
  "appName": "cache-service",
  "environment": "prod",
  "configKeys": ["redis.cluster.nodes"],
  "grayStrategy": "CANARY",
  "grayRules": {
    "canary": {
      "canaryInstances": ["192.168.1.200"],
      "stepPercentage": 20,
      "stepInterval": 30
    }
  }
}
```

## API接口

### 1. 创建灰度发布计划

```http
POST /api/gray-release/plan
Content-Type: application/json

{
  "planName": "数据库配置灰度发布",
  "appName": "demo-app",
  "environment": "prod",
  "groupName": "database",
  "configKeys": ["database.url", "database.username"],
  "grayStrategy": "IP_WHITELIST",
  "grayRules": {
    "ipWhitelist": ["192.168.1.100", "192.168.1.101"]
  },
  "description": "数据库配置优化"
}
```

### 2. 开始灰度发布

```http
POST /api/gray-release/plan/{planId}/start
```

### 3. 获取灰度配置

```http
GET /api/gray-release/config/value?appName=demo-app&environment=prod&groupName=database&configKey=database.url&instanceId=instance-001&instanceIp=192.168.1.100
```

## 前端界面

### 1. 灰度发布管理页面

- 发布计划列表
- 创建发布计划对话框
- 发布计划详情查看
- 操作按钮（开始、暂停、恢复、完成、回滚）

### 2. 核心功能

- **计划管理**: 创建、查看、编辑发布计划
- **状态监控**: 实时查看发布状态和进度
- **操作控制**: 支持暂停、恢复、完成、回滚等操作
- **详情查看**: 查看每个配置项的灰度状态

## 实现原理

### 1. 配置获取流程

```
客户端请求配置
    ↓
检查是否有进行中的灰度发布
    ↓
根据灰度策略判断是否命中灰度
    ↓
返回对应的配置值（灰度值或原值）
```

### 2. 灰度策略判断

- **IP白名单**: 检查请求IP是否在白名单中
- **按比例发布**: 使用一致性哈希算法确保稳定性
- **金丝雀发布**: 按照预设的实例列表和比例进行判断

### 3. 状态管理

- **DRAFT**: 草稿状态，可以编辑
- **ACTIVE**: 进行中，部分实例获取灰度配置
- **PAUSED**: 已暂停，停止新的灰度分发
- **COMPLETED**: 已完成，所有实例都获取新配置
- **ROLLBACK**: 已回滚，所有实例恢复原配置

## 最佳实践

### 1. 发布前准备

- 充分测试配置变更
- 准备回滚方案
- 设置监控告警
- 通知相关人员

### 2. 灰度发布过程

- 选择合适的灰度策略
- 设置合理的灰度比例
- 密切监控系统指标
- 及时响应异常情况

### 3. 发布后验证

- 验证配置是否生效
- 检查系统运行状况
- 收集用户反馈
- 记录发布经验

## 监控和告警

### 1. 关键指标

- 错误率变化
- 响应时间变化
- 系统资源使用率
- 业务指标变化

### 2. 自动回滚条件

- 错误率超过阈值
- 响应时间超过阈值
- 系统资源耗尽
- 业务指标异常

### 3. 告警通知

- 邮件通知
- 短信通知
- 钉钉/企业微信通知
- 系统内消息

## 总结

灰度发布功能为配置中心提供了安全、可控的配置发布能力，通过多种灰度策略和完善的监控机制，可以有效降低配置变更的风险，提高系统的稳定性和可靠性。

该功能适用于各种规模的系统，从小型应用到大型分布式系统都可以从中受益。通过合理使用灰度发布，可以实现配置的平滑升级，确保业务的连续性和稳定性。 