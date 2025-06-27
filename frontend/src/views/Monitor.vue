<template>
<<<<<<< HEAD
  <div class="monitor-container">
    <el-card>
      <template #header>
        <span>系统监控</span>
      </template>
      <div>系统监控页面 - 开发中...</div>
    </el-card>
=======
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
              <div class="stat-label">CPU使用率</div>
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
              <div class="stat-label">内存使用率</div>
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
              <div class="stat-label">配置项总数</div>
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
              <div class="stat-label">在线用户</div>
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
            <span>系统性能监控</span>
          </template>
          <div ref="performanceChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>配置操作统计</span>
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
            <span>服务状态</span>
          </template>
          <el-table :data="serviceList" style="width: 100%">
            <el-table-column prop="name" label="服务名称" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'UP' ? 'success' : 'danger'">
                  {{ scope.row.status === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" />
            <el-table-column prop="uptime" label="运行时间" />
            <el-table-column prop="lastCheck" label="最后检查时间" />
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
              <span>系统日志</span>
              <el-button @click="refreshLogs">
                <el-icon><Refresh /></el-icon>
                刷新
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
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
// 系统监控页面
</script>

<style scoped lang="scss">
.monitor-container {
  padding: 20px;
}
=======
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Cpu, Monitor, Setting, User, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const systemStats = reactive({
  cpuUsage: 0,
  memoryUsage: 0,
  configCount: 0,
  onlineUsers: 0
})

const serviceList = ref([
  { name: 'MySQL', status: 'UP', version: '8.0.33', uptime: '7天3小时', lastCheck: '2024-01-20 10:30:00' },
  { name: 'Redis', status: 'UP', version: '7.0.8', uptime: '7天3小时', lastCheck: '2024-01-20 10:30:00' },
  { name: 'ZooKeeper', status: 'UP', version: '3.8.1', uptime: '7天3小时', lastCheck: '2024-01-20 10:30:00' }
])

const logList = ref([
  { id: 1, timestamp: '2024-01-20 10:30:15', level: 'INFO', message: '用户admin登录系统' },
  { id: 2, timestamp: '2024-01-20 10:29:45', level: 'INFO', message: '配置项user.service.timeout更新成功' },
  { id: 3, timestamp: '2024-01-20 10:28:30', level: 'WARN', message: 'ZooKeeper连接超时，正在重试...' },
  { id: 4, timestamp: '2024-01-20 10:27:12', level: 'ERROR', message: '数据库连接池满载，请检查连接泄漏' }
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
      text: '系统性能趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['CPU使用率', '内存使用率']
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
        name: 'CPU使用率',
        type: 'line',
        data: [12, 15, 18, 22, 25, 28],
        smooth: true
      },
      {
        name: '内存使用率',
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
      text: '操作类型分布',
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
          { value: 35, name: '查询' },
          { value: 28, name: '更新' },
          { value: 20, name: '新增' },
          { value: 17, name: '删除' }
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
  console.log('刷新日志')
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
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
</style> 