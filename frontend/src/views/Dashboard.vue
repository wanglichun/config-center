<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalConfigs }}</div>
              <div class="stat-label">{{ $t('dashboard.totalConfigs') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon apps">
              <el-icon size="24"><Grid /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalApps }}</div>
              <div class="stat-label">{{ $t('dashboard.totalApps') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon envs">
              <el-icon size="24"><Platform /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalEnvironments }}</div>
              <div class="stat-label">{{ $t('dashboard.totalEnvironments') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon changes">
              <el-icon size="24"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.recentChanges }}</div>
              <div class="stat-label">{{ $t('dashboard.recentChanges') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.configStatusDistribution') }}</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="configStatusOption" style="height: 300px;" />
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.envConfigDistribution') }}</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="envConfigOption" style="height: 300px;" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.recentActivity') }}</span>
              <el-button text @click="$router.push('/history')">
                {{ $t('dashboard.viewAll') }}
              </el-button>
            </div>
          </template>
          <div class="activity-list">
            <div v-if="recentActivities.length === 0" class="empty-state">
              <el-icon class="empty-icon"><Document /></el-icon>
              <div class="empty-text">{{ $t('dashboard.noActivityRecords') }}</div>
            </div>
            <div
              v-for="activity in recentActivities"
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-icon">
                <el-icon>
                  <Edit v-if="activity.operationType === 'UPDATE'" />
                  <Plus v-else-if="activity.operationType === 'CREATE'" />
                  <Delete v-else-if="activity.operationType === 'DELETE'" />
                  <Upload v-else-if="activity.operationType === 'PUBLISH'" />
                  <RefreshLeft v-else />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">
                  {{ getOperationText(activity.operationType) }}{{ $t('config.title') }}
                  <el-tag size="small" class="ml-10">{{ activity.appName }}</el-tag>
                </div>
                <div class="activity-desc">
                  {{ activity.configKey }} - {{ activity.environment }}
                </div>
                <div class="activity-time">
                  {{ formatTime(activity.operateTime) }}
                </div>
              </div>
              <div class="activity-operator">
                {{ activity.operator }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.quickActions') }}</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button
              type="primary"
              size="large"
              class="action-btn"
              @click="$router.push('/config')"
            >
              <el-icon><Plus /></el-icon>
              {{ $t('dashboard.newConfig') }}
            </el-button>
            <el-button
              size="large"
              class="action-btn"
              @click="$router.push('/config')"
            >
              <el-icon><Search /></el-icon>
              {{ $t('dashboard.viewConfigs') }}
            </el-button>
            <el-button
              size="large"
              class="action-btn"
              @click="$router.push('/history')"
            >
              <el-icon><Clock /></el-icon>
              {{ $t('dashboard.changeHistory') }}
            </el-button>
            <el-button
              v-if="userStore.userInfo?.role === 'ADMIN'"
              size="large"
              class="action-btn"
              @click="$router.push('/users')"
            >
              <el-icon><User /></el-icon>
              {{ $t('dashboard.userManagement') }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import type { ConfigStats } from '@/types/config'
import type { ConfigHistory } from '@/types/config'
// 导入图标
import {
  Document,
  Grid,
  Platform,
  Clock,
  Plus,
  Search,
  User,
  Edit,
  Delete,
  Upload,
  RefreshLeft
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const userStore = useUserStore()
const { t } = useI18n()

// 统计数据
const stats = ref<ConfigStats>({
  totalConfigs: 0,
  totalApps: 0,
  totalEnvironments: 0,
  recentChanges: 0,
  publishedConfigs: 0,
  draftConfigs: 0
})

// 最近活动
const recentActivities = ref<ConfigHistory[]>([])

// 配置状态分布图表
const configStatusOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: t('dashboard.configStatus'),
      type: 'pie',
      radius: '50%',
      data: [
        { value: 0, name: t('status.published') },
        { value: 0, name: t('status.draft') },
        { value: 0, name: t('status.disabled') }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 环境配置分布图表
const envConfigOption = ref({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      data: ['dev', 'test', 'prod'],
      axisTick: {
        alignWithLabel: true
      }
    }
  ],
  yAxis: [
    {
      type: 'value'
    }
  ],
  series: [
    {
      name: t('dashboard.configCount'),
      type: 'bar',
      barWidth: '60%',
      data: [0, 0, 0],
      itemStyle: {
        color: '#409EFF'
      }
    }
  ]
})

// 获取操作文本
const getOperationText = (type: string) => {
  const map: Record<string, string> = {
    CREATE: t('operation.create'),
    UPDATE: t('operation.update'),
    DELETE: t('operation.delete'),
    PUBLISH: t('operation.publish'),
    ROLLBACK: t('operation.rollback')
  }
  return map[type] || t('operation.operation')
}

// 格式化时间
const formatTime = (timestamp: number) => {
  return dayjs(timestamp).fromNow()
}

// 加载统计数据
const loadStats = async () => {
  try {
    // 模拟数据，实际应该调用API
    stats.value = {
      totalConfigs: 156,
      totalApps: 12,
      totalEnvironments: 3,
      recentChanges: 8,
      publishedConfigs: 128,
      draftConfigs: 28
    }
    
    // 更新图表数据
    configStatusOption.value.series[0].data = [
      { value: stats.value.publishedConfigs, name: t('status.published') },
      { value: stats.value.draftConfigs, name: t('status.draft') },
      { value: stats.value.totalConfigs - stats.value.publishedConfigs - stats.value.draftConfigs, name: t('status.disabled') }
    ]
    
    envConfigOption.value.series[0].data = [65, 48, 43]
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最近活动
const loadRecentActivities = async () => {
  try {
    // 模拟数据，实际应该调用API
    recentActivities.value = [
      {
        id: 1,
        configId: 1,
        appName: 'user-service',
        environment: 'prod',
        groupName: 'database',
        configKey: 'db.host',
        oldValue: 'localhost',
        newValue: 'prod-db.example.com',
        version: 2,
        operationType: 'UPDATE',
        operator: 'admin',
        operateTime: Date.now() - 1000 * 60 * 30, // 30分钟前
        rollback: false,
        createTime: new Date().toISOString()
      },
      {
        id: 2,
        configId: 2,
        appName: 'order-service',
        environment: 'test',
        groupName: 'redis',
        configKey: 'redis.timeout',
        oldValue: '',
        newValue: '5000',
        version: 1,
        operationType: 'CREATE',
        operator: 'developer',
        operateTime: Date.now() - 1000 * 60 * 60 * 2, // 2小时前
        rollback: false,
        createTime: new Date().toISOString()
      }
    ]
  } catch (error) {
    console.error('加载最近活动失败:', error)
  }
}

onMounted(() => {
  loadStats()
  loadRecentActivities()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stats-cards {
    margin-bottom: 20px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          
          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }
          
          &.apps {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }
          
          &.envs {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
          }
          
          &.changes {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            color: white;
          }
        }
        
        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: 600;
            color: #333;
            line-height: 1;
          }
          
          .stat-label {
            font-size: 14px;
            color: #666;
            margin-top: 4px;
          }
        }
      }
    }
  }
  
  .charts-section {
    margin-bottom: 20px;
    
    .chart-card {
      .card-header {
        font-weight: 600;
        color: #333;
      }
      
      .chart-container {
        padding: 10px 0;
      }
    }
  }
  
  .activity-card {
    .activity-list {
      .activity-item {
        display: flex;
        align-items: flex-start;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .activity-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          background: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          color: #409EFF;
        }
        
        .activity-content {
          flex: 1;
          
          .activity-title {
            font-size: 14px;
            color: #333;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
          }
          
          .activity-desc {
            font-size: 13px;
            color: #666;
            margin-bottom: 4px;
          }
          
          .activity-time {
            font-size: 12px;
            color: #999;
          }
        }
        
        .activity-operator {
          font-size: 13px;
          color: #666;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
        }
      }
    }
  }
  
  .quick-actions-card {
    .quick-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .action-btn {
        width: 100%;
        justify-content: flex-start;
        
        .el-icon {
          margin-right: 8px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    .stats-cards {
      .stat-card {
        margin-bottom: 16px;
        
        .stat-content {
          .stat-icon {
            width: 50px;
            height: 50px;
            margin-right: 12px;
          }
          
          .stat-info {
            .stat-value {
              font-size: 24px;
            }
          }
        }
      }
    }
    
    .charts-section {
      .chart-card {
        margin-bottom: 16px;
      }
    }
  }
}
</style> 