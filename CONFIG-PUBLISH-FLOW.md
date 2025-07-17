# 配置发布流程设计文档

## 概述

配置发布是配置中心的核心功能，本文档详细描述了配置发布的完整流程，包括直接发布和灰度发布两种模式。

## 发布流程架构

### 1. 整体流程

```
配置管理 → 发布选择 → 发布执行 → 状态更新 → 历史记录
    ↓
├── 直接发布：立即发布到所有实例
└── 灰度发布：逐步发布到部分实例
```

### 2. 发布模式对比

| 特性 | 直接发布 | 灰度发布 |
|------|----------|----------|
| 发布速度 | 立即生效 | 逐步生效 |
| 风险控制 | 低 | 高 |
| 适用场景 | 测试环境、非关键配置 | 生产环境、关键配置 |
| 回滚难度 | 中等 | 容易 |
| 监控复杂度 | 简单 | 复杂 |

## 直接发布流程

### 1. 前端流程

```typescript
// 配置列表页面
const handlePublish = async (row: ConfigItem) => {
  // 1. 显示发布选项对话框
  await ElMessageBox.confirm(
    t('config.messages.publishConfirm', { key: row.configKey }),
    t('config.publish.title'),
    {
      confirmButtonText: t('config.publish.directPublish'),
      cancelButtonText: t('config.publish.grayPublish'),
      // ...
    }
  )
  
  // 2. 执行直接发布
  await directPublish(row)
}

// 直接发布实现
const directPublish = async (row: ConfigItem) => {
  const response = await publishConfig(row.id)
  if (response.success) {
    ElMessage.success(t('config.messages.publishSuccess'))
    loadConfigList() // 刷新列表
  }
}
```

### 2. 后端流程

```java
@PostMapping("/{id}/publish")
@PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
public ApiResult<Boolean> publishConfig(@PathVariable Long id, HttpServletRequest request) {
    String publisher = getCurrentUser(request);
    boolean result = configService.publishConfig(id, publisher);
    return result ? ApiResult.success(true) : ApiResult.error("发布配置失败");
}

// 发布配置服务实现
@Override
@Transactional
@CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
public boolean publishConfig(Long id, String publisher) {
    try {
        // 1. 获取配置项
        ConfigItem configItem = configItemMapper.findById(id);
        if (configItem == null) {
            return false;
        }
        
        // 2. 解密配置值（如果需要）
        String configValue = configItem.getConfigValue();
        if (Boolean.TRUE.equals(configItem.getEncrypted())) {
            configValue = decryptConfig(configValue);
        }
        
        // 3. 发布到ZooKeeper
        boolean published = zooKeeperService.publishConfig(configItem.getZkPath(), configValue);
        
        if (published) {
            // 4. 更新发布信息
            configItem.setLastPublishTime(System.currentTimeMillis());
            configItem.setPublisher(publisher);
            configItem.setUpdateTime(LocalDateTime.now());
            configItemMapper.update(configItem);
            
            // 5. 记录历史
            recordHistory(configItem, "PUBLISH", null, configValue, 
                        "发布配置", publisher);
            
            return true;
        }
        
        return false;
    } catch (Exception e) {
        log.error("发布配置失败", e);
        return false;
    }
}
```

## 灰度发布流程

### 1. 灰度发布策略

#### IP白名单策略
```json
{
  "strategy": "IP_WHITELIST",
  "rules": {
    "ipWhitelist": ["192.168.1.100", "192.168.1.101"]
  }
}
```

#### 按比例发布策略
```json
{
  "strategy": "PERCENTAGE",
  "rules": {
    "percentage": 10
  }
}
```

#### 金丝雀发布策略
```json
{
  "strategy": "CANARY",
  "rules": {
    "canaryInstances": ["instance-001", "instance-002"],
    "stepPercentage": 20,
    "stepInterval": 30
  }
}
```

### 2. 前端灰度发布流程

```typescript
// 灰度发布实现
const grayPublish = async (row: ConfigItem) => {
  // 跳转到灰度发布页面，并传递配置信息
  router.push({
    name: 'GrayRelease',
    query: {
      appName: row.appName,
      environment: row.environment,
      groupName: row.groupName,
      configKey: row.configKey,
      prefill: 'true'
    }
  })
}

// 灰度发布页面 - 创建计划
const createPlan = async () => {
  const planData = {
    planName: createForm.planName,
    appName: createForm.appName,
    environment: createForm.environment,
    groupName: createForm.groupName,
    configKeys: [createForm.configKey],
    grayStrategy: createForm.strategy,
    grayRules: createForm.grayRules,
    description: createForm.description
  }
  
  const response = await grayReleaseApi.createPlan(planData)
  if (response.success) {
    ElMessage.success(t('grayRelease.createSuccess'))
    loadPlans()
  }
}

// 执行灰度发布
const executePlan = async (plan: any) => {
  const response = await grayReleaseApi.executePlan(plan.id)
  if (response.success) {
    ElMessage.success(t('grayRelease.executeSuccess'))
    loadPlans()
  }
}
```

### 3. 后端灰度发布流程

```java
// 灰度发布服务
@Service
public class GrayReleaseServiceImpl implements GrayReleaseService {
    
    @Override
    @Transactional
    public void startGrayRelease(Long planId, String operator) {
        try {
            // 1. 获取灰度发布计划
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            
            // 2. 更新计划状态
            plan.setStatus("ACTIVE");
            plan.setStartTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 3. 推送灰度配置
            configPushService.pushGrayConfig(planId);
            
            // 4. 通知相关实例刷新配置
            notifyInstancesRefresh(planId);
            
        } catch (Exception e) {
            log.error("开始灰度发布失败", e);
            throw new RuntimeException("开始灰度发布失败: " + e.getMessage());
        }
    }
    
    // 推送灰度配置
    private void pushGrayConfig(Long planId) {
        GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
        List<GrayReleaseDetail> details = grayReleaseDetailMapper.selectByPlanId(planId);
        
        String strategy = plan.getGrayStrategy();
        switch (strategy) {
            case "IP_WHITELIST":
                pushIpWhitelistConfig(plan, details);
                break;
            case "PERCENTAGE":
                pushPercentageConfig(plan, details);
                break;
            case "CANARY":
                pushCanaryConfig(plan, details);
                break;
        }
    }
}
```

## 批量发布功能

### 1. 批量操作界面

```vue
<!-- 批量操作工具栏 -->
<div v-if="selectedConfigs.length > 0" class="batch-toolbar">
  <el-alert
    :title="$t('config.batch.selectedCount', { count: selectedConfigs.length })"
    type="info"
    :closable="false"
    show-icon
  >
    <template #default>
      <div class="batch-actions">
        <el-button size="small" type="primary" @click="handleBatchPublish">
          <el-icon><Upload /></el-icon>
          {{ $t('config.batch.publish') }}
        </el-button>
        <el-button size="small" type="success" @click="handleBatchGrayPublish">
          <el-icon><Share /></el-icon>
          {{ $t('config.batch.grayPublish') }}
        </el-button>
        <el-button size="small" @click="clearSelection">
          {{ $t('config.batch.clearSelection') }}
        </el-button>
      </div>
    </template>
  </el-alert>
</div>
```

### 2. 批量发布实现

```typescript
// 批量发布
const handleBatchPublish = async () => {
  if (selectedConfigs.value.length === 0) {
    ElMessage.warning(t('config.batch.noConfigsSelected'))
    return
  }

  try {
    await ElMessageBox.confirm(
      t('config.batch.batchPublishConfirm', { count: selectedConfigs.value.length }),
      t('config.batch.batchPublishTitle'),
      {
        confirmButtonText: t('config.batch.batchPublishConfirmButton'),
        cancelButtonText: t('common.cancel'),
        type: 'info'
      }
    )
    
    // 并行执行所有发布操作
    await Promise.all(selectedConfigs.value.map(config => directPublish(config)))
    
    ElMessage.success(t('config.messages.batchPublishSuccess'))
    selectedConfigs.value = [] // 清空选择
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发布失败:', error)
      ElMessage.error(t('config.messages.batchPublishFailed'))
    }
  }
}
```

## 配置详情页面

### 1. 页面功能

- **配置基本信息展示**：应用名称、环境、配置组、配置键等
- **配置值管理**：查看、编辑、复制配置值
- **发布状态监控**：最后发布时间、发布者、更新时间
- **发布历史记录**：查看所有操作历史，支持历史详情对比

### 2. 核心功能实现

```vue
<template>
  <div class="config-detail-page">
    <!-- 配置基本信息 -->
    <el-descriptions :column="2" border>
      <el-descriptions-item :label="$t('config.appName')">
        {{ configDetail.appName }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('config.environment')">
        <el-tag>{{ getEnvironmentLabel(configDetail.environment) }}</el-tag>
      </el-descriptions-item>
      <!-- ... 其他字段 -->
    </el-descriptions>

    <!-- 配置值 -->
    <div class="config-value-section">
      <h3>{{ $t('config.detail.configValue') }}</h3>
      <div class="config-value-display">
        <el-input
          v-model="configDetail.configValue"
          type="textarea"
          :rows="6"
          readonly
          :show-password="configDetail.encrypted"
        />
        <div class="config-value-actions">
          <el-button size="small" @click="copyConfigValue">
            <el-icon><CopyDocument /></el-icon>
            {{ $t('common.copy') }}
          </el-button>
          <el-button size="small" @click="editConfigValue">
            <el-icon><Edit /></el-icon>
            {{ $t('common.edit') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 发布状态 -->
    <div class="publish-status-section">
      <h3>{{ $t('config.detail.publishStatus') }}</h3>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="status-card">
            <template #header>
              <div class="status-card-header">
                <el-icon><Clock /></el-icon>
                <span>{{ $t('config.detail.lastPublishTime') }}</span>
              </div>
            </template>
            <div class="status-card-content">
              {{ configDetail.lastPublishTime ? formatTime(configDetail.lastPublishTime) : $t('config.detail.neverPublished') }}
            </div>
          </el-card>
        </el-col>
        <!-- ... 其他状态卡片 -->
      </el-row>
    </div>

    <!-- 发布历史 -->
    <div class="publish-history-section">
      <h3>{{ $t('config.detail.publishHistory') }}</h3>
      <el-table :data="publishHistory" v-loading="historyLoading">
        <el-table-column prop="operationType" :label="$t('config.detail.operationType')" width="120">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeLabel(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- ... 其他列 -->
      </el-table>
    </div>
  </div>
</template>
```

## 发布监控和告警

### 1. 监控指标

- **发布成功率**：成功发布次数 / 总发布次数
- **发布耗时**：从发布到生效的时间
- **配置生效率**：实际生效的实例数 / 目标实例数
- **错误率变化**：发布前后的错误率对比

### 2. 告警规则

```yaml
# 发布失败告警
- alert: ConfigPublishFailed
  expr: config_publish_failed_total > 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "配置发布失败"
    description: "配置 {{ $labels.config_key }} 发布失败"

# 发布耗时过长告警
- alert: ConfigPublishSlow
  expr: config_publish_duration_seconds > 30
  for: 1m
  labels:
    severity: warning
  annotations:
    summary: "配置发布耗时过长"
    description: "配置 {{ $labels.config_key }} 发布耗时超过30秒"
```

## 最佳实践

### 1. 发布前准备

- **充分测试**：在测试环境验证配置变更
- **备份配置**：发布前备份当前配置
- **通知相关人员**：提前通知可能受影响的团队
- **准备回滚方案**：制定详细的回滚计划

### 2. 发布过程

- **选择合适的发布策略**：根据配置重要性和风险程度选择
- **监控关键指标**：密切关注系统运行状况
- **及时响应异常**：发现问题立即处理
- **记录发布过程**：详细记录发布步骤和结果

### 3. 发布后验证

- **验证配置生效**：确认配置已正确应用到目标实例
- **检查系统运行**：监控系统性能和稳定性
- **收集用户反馈**：关注用户使用体验
- **总结发布经验**：记录成功和失败的经验教训

## 总结

配置发布流程是配置中心的核心功能，通过直接发布和灰度发布两种模式，为用户提供了灵活、安全的配置发布能力。整个流程涵盖了前端交互、后端处理、状态监控、历史记录等各个环节，确保了配置发布的可靠性和可追溯性。

通过批量发布功能，用户可以高效地管理多个配置的发布；通过配置详情页面，用户可以深入了解配置的发布状态和历史；通过监控和告警机制，系统能够及时发现和处理发布过程中的问题。

这种设计既满足了不同场景下的发布需求，又保证了配置发布的安全性和可控性，为配置中心的高效运行提供了强有力的支撑。 