# 🚀 分层分组发布流程设计

## 📋 概述

分层分组发布是一种更安全、更可控的配置发布策略，通过将订阅配置的机器分成3层进行逐步发布，确保配置变更的安全性和可回滚性。

## 🏗️ 架构设计

### 1. 整体流程架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      分层分组发布流程                              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   1. 拉取配置订阅机器列表                         │
│   • 根据 appName、environment、configKey 查询                    │
│   • 获取所有订阅此配置的机器实例                                   │
│   • 过滤掉离线或异常的机器                                        │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   2. 机器分层分组策略                             │
│   第一层 (金丝雀层): 5%  - 1-2台机器，验证配置正确性              │
│   第二层 (灰度层):  25% - 剩余机器的30%，小范围验证               │
│   第三层 (全量层):  70% - 剩余所有机器，全量发布                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   3. 分层发布执行流程                             │
│   Layer 1 → 验证 → Layer 2 → 验证 → Layer 3 → 完成               │
│   每层间隔可配置，支持人工干预和自动回滚                           │
└─────────────────────────────────────────────────────────────────┘
```

### 2. 机器分层算法

```java
/**
 * 机器分层分组算法
 */
public class LayeredReleaseStrategy {
    
    // 分层配置
    private static final double LAYER_1_PERCENTAGE = 0.05;  // 5%
    private static final double LAYER_2_PERCENTAGE = 0.25;  // 25%
    private static final double LAYER_3_PERCENTAGE = 0.70;  // 70%
    
    // 最小机器数量保证
    private static final int MIN_LAYER_1_COUNT = 1;
    private static final int MIN_LAYER_2_COUNT = 2;
    
    public LayeredMachineGroups splitMachines(List<MachineInstance> machines) {
        int totalCount = machines.size();
        
        // 计算每层机器数量
        int layer1Count = Math.max(MIN_LAYER_1_COUNT, (int)(totalCount * LAYER_1_PERCENTAGE));
        int layer2Count = Math.max(MIN_LAYER_2_COUNT, (int)(totalCount * LAYER_2_PERCENTAGE));
        int layer3Count = totalCount - layer1Count - layer2Count;
        
        // 分层策略：优先选择稳定性较高的机器作为第一层
        Collections.sort(machines, new MachineStabilityComparator());
        
        return LayeredMachineGroups.builder()
            .layer1(machines.subList(0, layer1Count))
            .layer2(machines.subList(layer1Count, layer1Count + layer2Count))
            .layer3(machines.subList(layer1Count + layer2Count, totalCount))
            .build();
    }
}
```

## 🔄 发布流程详细设计

### 1. 创建分层发布计划

```java
@PostMapping("/api/layered-release/plan")
public ApiResult<Long> createLayeredPlan(@RequestBody LayeredReleaseRequest request) {
    // 1. 拉取配置订阅机器列表
    List<MachineInstance> subscribedMachines = machineGroupService
        .getSubscribedMachines(request.getAppName(), request.getEnvironment(), request.getConfigKeys());
    
    // 2. 机器分层分组
    LayeredMachineGroups machineGroups = layeredReleaseStrategy.splitMachines(subscribedMachines);
    
    // 3. 创建发布计划
    LayeredReleasePlan plan = LayeredReleasePlan.builder()
        .planName(request.getPlanName())
        .appName(request.getAppName())
        .environment(request.getEnvironment())
        .configKeys(request.getConfigKeys())
        .totalMachines(subscribedMachines.size())
        .layer1Machines(machineGroups.getLayer1())
        .layer2Machines(machineGroups.getLayer2())
        .layer3Machines(machineGroups.getLayer3())
        .currentLayer(1)
        .status("DRAFT")
        .autoAdvance(request.isAutoAdvance())
        .layerInterval(request.getLayerInterval()) // 层间间隔时间
        .build();
    
    return ApiResult.success(layeredReleasePlanService.createPlan(plan));
}
```

### 2. 分层发布执行流程

```java
@Service
public class LayeredReleaseExecutor {
    
    @Async
    public void executeLayeredRelease(Long planId) {
        LayeredReleasePlan plan = layeredReleasePlanService.getPlan(planId);
        
        try {
            // 第一层发布
            executeLayer1(plan);
            
            // 验证第一层
            if (validateLayer(plan, 1)) {
                // 等待层间间隔
                Thread.sleep(plan.getLayerInterval() * 1000);
                
                // 第二层发布
                executeLayer2(plan);
                
                // 验证第二层
                if (validateLayer(plan, 2)) {
                    // 等待层间间隔
                    Thread.sleep(plan.getLayerInterval() * 1000);
                    
                    // 第三层发布
                    executeLayer3(plan);
                    
                    // 验证第三层
                    if (validateLayer(plan, 3)) {
                        completePlan(plan);
                    }
                }
            }
        } catch (Exception e) {
            log.error("分层发布执行失败", e);
            handleReleaseFailure(plan, e);
        }
    }
}
```

## 📊 核心特性

1. **智能分层**: 根据机器稳定性和负载情况智能分层
2. **渐进式发布**: 从小范围到大范围逐步推进
3. **自动验证**: 每层发布后自动验证配置生效情况
4. **快速回滚**: 支持任意层级的快速回滚
5. **实时监控**: 全程监控发布进度和机器状态
6. **人工干预**: 支持手动控制发布节奏

## 🎯 应用场景

- **数据库配置变更**: 连接池、超时设置等关键配置
- **缓存配置优化**: Redis集群、缓存策略调整
- **功能开关发布**: 新功能的渐进式启用
- **性能参数调优**: JVM参数、线程池配置等
- **安全配置更新**: 认证、授权相关配置变更

这个分层分组发布设计提供了一个完整的、可控的、安全的配置发布方案，能够有效降低配置变更的风险，提高发布的成功率。 