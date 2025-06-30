<template>
  <div class="monitor-page">
    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview">
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon cpu">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ systemStats.cpuUsage }}%</div>
              <div class="stat-label">{{ $t('monitor.cpuUsage') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon memory">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ systemStats.memoryUsage }}%</div>
              <div class="stat-label">{{ $t('monitor.memoryUsage') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon config">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ systemStats.configCount }}</div>
              <div class="stat-label">{{ $t('monitor.totalConfigs') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon user">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ systemStats.onlineUsers }}</div>
              <div class="stat-label">{{ $t('monitor.onlineUsers') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>{{ $t('monitor.performanceMonitor') }}</span>
          </template>
          <div ref="performanceChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>{{ $t('monitor.operationStats') }}</span>
          </template>
          <div ref="operationChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 服务状态 -->
    <el-row :gutter="20" class="services">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>{{ $t('monitor.serviceStatus') }}</span>
          </template>
          <el-table :data="serviceList" style="width: 100%">
            <el-table-column prop="name" :label="$t('monitor.serviceName')" />
            <el-table-column prop="status" :label="$t('monitor.status')">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'UP' ? 'success' : 'danger'">
                  {{ scope.row.status === 'UP' ? $t('monitor.normal') : $t('monitor.abnormal') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="version" :label="$t('monitor.version')" />
            <el-table-column prop="uptime" :label="$t('monitor.uptime')" />
            <el-table-column prop="lastCheck" :label="$t('monitor.lastCheck')" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志监控 -->
    <el-row :gutter="20" class="logs">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ $t('monitor.systemLogs') }}</span>
              <el-button @click="refreshLogs">
                <el-icon><Refresh /></el-icon>
                {{ $t('monitor.refresh') }}
              </el-button>
            </div>
          </template>
          <div class="log-container">
            <div 
              v-for="log in logList" 
              :key="log.id" 
              :class="['log-item', `log-${log.level.toLowerCase()}`]"
            >
              <span class="log-time">{{ log.timestamp }}</span>
              <span class="log-level">{{ log.level }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Cpu, Monitor, Setting, User, Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'

const { t } = useI18n()

const systemStats = reactive({
  cpuUsage: 0,
  memoryUsage: 0,
  configCount: 0,
  onlineUsers: 0
})

const serviceList = ref([
  { name: 'MySQL', status: 'UP', version: '8.0.33', uptime: t('monitor.days7Hours3'), lastCheck: '2024-01-20 10:30:00' },
  { name: 'Redis', status: 'UP', version: '7.0.8', uptime: t('monitor.days7Hours3'), lastCheck: '2024-01-20 10:30:00' },
  { name: 'ZooKeeper', status: 'UP', version: '3.8.1', uptime: t('monitor.days7Hours3'), lastCheck: '2024-01-20 10:30:00' }
])

const logList = ref([
  { id: 1, timestamp: '2024-01-20 10:30:15', level: 'INFO', message: t('monitor.userLoginLog') },
  { id: 2, timestamp: '2024-01-20 10:29:45', level: 'INFO', message: t('monitor.configUpdateLog') },
  { id: 3, timestamp: '2024-01-20 10:28:30', level: 'WARN', message: t('monitor.zkTimeoutLog') },
  { id: 4, timestamp: '2024-01-20 10:27:12', level: 'ERROR', message: t('monitor.dbConnectionLog') }
])

const performanceChart = ref()
const operationChart = ref()
let performanceChartInstance: echarts.ECharts
let operationChartInstance: echarts.ECharts
let timer: NodeJS.Timeout

const initPerformanceChart = () => {
  performanceChartInstance = echarts.init(performanceChart.value)
  
  const option = {
    title: {
      text: t('monitor.performanceTrend')
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: [t('monitor.cpuUsage'), t('monitor.memoryUsage')]
    },
    xAxis: {
      type: 'category',
      data: ['10:25', '10:26', '10:27', '10:28', '10:29', '10:30']
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: t('monitor.cpuUsage'),
        type: 'line',
        data: [12, 15, 18, 22, 25, 28],
        smooth: true
      },
      {
        name: t('monitor.memoryUsage'),
        type: 'line',
        data: [45, 47, 50, 52, 55, 58],
        smooth: true
      }
    ]
  }
  
  performanceChartInstance.setOption(option)
}

const initOperationChart = () => {
  operationChartInstance = echarts.init(operationChart.value)
  
  const option = {
    title: {
      text: t('monitor.operationDistribution'),
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 35, name: t('monitor.query') },
          { value: 28, name: t('monitor.update') },
          { value: 20, name: t('monitor.create') },
          { value: 17, name: t('monitor.delete') }
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
  }
  
  operationChartInstance.setOption(option)
}

const updateSystemStats = () => {
  // 模拟实时数据更新
  systemStats.cpuUsage = Math.floor(Math.random() * 30) + 20
  systemStats.memoryUsage = Math.floor(Math.random() * 20) + 50
  systemStats.configCount = 1234 + Math.floor(Math.random() * 10)
  systemStats.onlineUsers = 5 + Math.floor(Math.random() * 3)
}

const refreshLogs = () => {
  // TODO: 实现日志刷新功能
  console.log(t('monitor.refreshLogs'))
}

onMounted(() => {
  initPerformanceChart()
  initOperationChart()
  updateSystemStats()
  
  // 定时更新数据
  timer = setInterval(updateSystemStats, 5000)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    performanceChartInstance?.resize()
    operationChartInstance?.resize()
  })
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  performanceChartInstance?.dispose()
  operationChartInstance?.dispose()
})
</script>

<style scoped lang="scss">
.monitor-page {
  padding: 20px;
}

.overview {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
  
  &.cpu {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &.memory {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.config {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.user {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.charts {
  margin-bottom: 20px;
}

.services {
  margin-bottom: 20px;
}

.logs {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.log-item {
  display: flex;
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  
  &.log-info {
    background: #e1f3d8;
    border-left: 4px solid #67c23a;
  }
  
  &.log-warn {
    background: #fdf6ec;
    border-left: 4px solid #e6a23c;
  }
  
  &.log-error {
    background: #fef0f0;
    border-left: 4px solid #f56c6c;
  }
}

.log-time {
  color: #909399;
  margin-right: 10px;
  min-width: 120px;
}

.log-level {
  font-weight: bold;
  margin-right: 10px;
  min-width: 50px;
}

.log-message {
  flex: 1;
}
</style>