<template>
  <div class="ticket-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('ticket.detail.title') }}</span>
          <div class="actions">
            <el-button @click="goBack" icon="ArrowLeft">{{ $t('common.back') }}</el-button>
            <el-button @click="refreshData" icon="Refresh" :loading="loading">{{ $t('common.refresh') }}</el-button>
          </div>
        </div>
      </template>

      <!-- 工单状态进度条 -->
      <div class="status-progress" v-if="ticketDetail">
        <div class="progress-header">
          <h3>{{ $t('ticket.detail.statusProgress') }}</h3>
          <div class="action-buttons" v-if="ticketDetail?.action && ticketDetail.action.length > 0">
            <el-button 
              v-for="action in ticketDetail.action" 
              :key="action"
              :type="getActionButtonType(action)"
              :icon="getActionIcon(action)"
              @click="handleAction(action)"
              size="small"
            >
              {{ getActionText(action) }}
            </el-button>
          </div>
        </div>
        <div class="progress-container">
          <el-steps :active="getCurrentStepIndex()" finish-status="success" align-center>
            <el-step 
              v-for="(step, index) in getCurrentSteps()" 
              :key="index"
              :title="step.title"
              :description="step.description"
              :status="getStepStatus(index)"
            />
          </el-steps>
        </div>
      </div>

      <!-- 工单基础信息 -->
      <div class="basic-info">
        <h3>{{ $t('ticket.detail.basicInfo') }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('ticket.id')">
            {{ ticketDetail?.id }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.title')">
            {{ ticketDetail?.title }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.phase')">
            <el-tag :type="getPhaseTagType(ticketDetail?.phase)">
              {{ ticketDetail?.phase }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.applicator')">
            {{ ticketDetail?.applicator }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.operator')">
            {{ ticketDetail?.operator || $t('common.none') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.dataId')">
            {{ ticketDetail?.dataId }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.createTime')">
            {{ formatTime(ticketDetail?.createTime?.toString()) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.updateTime')">
            {{ formatTime(ticketDetail?.updateTime?.toString()) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.applicator')">
            {{ ticketDetail?.applicator || $t('common.none') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.operator')">
            {{ ticketDetail?.operator || $t('common.none') }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 数据对比 -->
      <div class="data-comparison">
        <h3>{{ $t('ticket.detail.dataComparison') }}</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="data-card">
              <template #header>
                <div class="data-header">
                  <span>{{ $t('ticket.detail.oldData') }}</span>
                  <el-button 
                    v-if="ticketDetail?.oldData" 
                    @click="copyOldData" 
                    size="small" 
                    icon="CopyDocument"
                  >
                    {{ $t('common.copy') }}
                  </el-button>
                </div>
              </template>
              <div class="data-content">
                <el-input
                  v-if="ticketDetail?.oldData"
                  :model-value="ticketDetail.oldData"
                  type="textarea"
                  :rows="8"
                  readonly
                  :show-password="isEncrypted"
                />
                <div v-else class="no-data">
                  {{ $t('ticket.detail.noOldData') }}
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="data-card">
              <template #header>
                <div class="data-header">
                  <span>{{ $t('ticket.detail.newData') }}</span>
                  <el-button 
                    v-if="ticketDetail?.newData" 
                    @click="copyNewData" 
                    size="small" 
                    icon="CopyDocument"
                  >
                    {{ $t('common.copy') }}
                  </el-button>
                </div>
              </template>
              <div class="data-content">
                <el-input
                  v-if="ticketDetail?.newData"
                  :model-value="ticketDetail.newData"
                  type="textarea"
                  :rows="8"
                  readonly
                  :show-password="isEncrypted"
                />
                <div v-else class="no-data">
                  {{ $t('ticket.detail.noNewData') }}
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 操作记录 -->
      <div class="operation-history">
        <h3>{{ $t('ticket.detail.operationHistory') }}</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(operation, index) in operationHistory"
            :key="index"
            :timestamp="formatTime(operation.time)"
            :type="getOperationType(operation.type)"
          >
            <h4>{{ operation.title }}</h4>
            <p>{{ operation.description }}</p>
            <p class="operator">{{ $t('ticket.detail.operator') }}: {{ operation.operator }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <!-- 灰度发布弹窗 -->
    <el-dialog
      v-model="grayPublishDialogVisible"
      :title="$t('ticket.detail.grayPublishDialog.title')"
      width="90%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      class="gray-publish-dialog"
    >
      <div class="gray-publish-content">
        <!-- 状态指示器 -->
        <div v-if="allMachinesSuccess" class="success-indicator" style="margin-bottom: 20px; padding: 15px; background-color: #f0f9ff; border: 1px solid #409eff; border-radius: 4px;">
          <el-alert
            title="所有机器状态都是Success"
            type="success"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>所有机器都已成功发布，可以点击下方的"Complete"按钮完成灰度发布。</p>
            </template>
          </el-alert>
        </div>
        
        <div class="machine-management">
          <!-- 左侧：可发布机器 -->
          <div class="left-panel">
            <div class="panel-header">
              <h4>{{ $t('ticket.detail.availableMachines') }} ({{ availableMachines.length }})</h4>
              <div class="header-actions">
                <el-button type="info" size="small" @click="refreshMachines" :loading="loading">
                  <el-icon><Refresh /></el-icon>
                  {{ $t('common.refresh') }}
                </el-button>
                <el-button type="primary" size="small" @click="publishToSelected" :disabled="selectedMachines.length === 0">
                  {{ $t('ticket.detail.publishToSelected') }}
                </el-button>
                <el-button type="success" size="small" @click="publishToAll" :disabled="availableMachines.filter(m => m.status === 'NoGray').length === 0">
                  {{ $t('ticket.detail.publishToAll') }}
                </el-button>
              </div>
            </div>

            <!-- 筛选器 -->
            <div class="filter-bar">
              <el-form :model="filterForm" inline size="small">
                <el-form-item :label="$t('ticket.detail.ip')">
                  <el-input v-model="filterForm.ip" :placeholder="$t('ticket.detail.searchIp')" clearable style="width: 120px;" />
                </el-form-item>
                <el-form-item :label="$t('ticket.detail.status')">
                  <el-select v-model="filterForm.status" :placeholder="$t('ticket.detail.status')" clearable style="width: 100px;">
                    <el-option label="Running" value="Running" />
                    <el-option label="Success" value="Success" />
                    <el-option label="NoGray" value="NoGray" />
                    <el-option label="Offline" value="Offline" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleFilter">{{ $t('common.filter') }}</el-button>
                  <el-button @click="resetFilter">{{ $t('common.reset') }}</el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 机器列表 -->
            <div class="machine-table">
              <el-table 
                :data="filteredAvailableMachines" 
                v-loading="loading"
                @selection-change="handleSelectionChange"
                style="width: 100%"
                height="300"
                stripe
                border
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="ip" :label="$t('ticket.detail.ip')" width="140">
                  <template #default="scope">
                    <div class="ip-cell">
                      <span>{{ scope.row.ip }}</span>
                      <el-icon class="copy-icon" @click="copyToClipboard(scope.row.ip)">
                        <CopyDocument />
                      </el-icon>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="status" :label="$t('ticket.detail.status')" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="version" :label="$t('ticket.detail.currentVersion')" width="120" />
                <el-table-column prop="lastUpdateTime" :label="$t('ticket.detail.lastUpdate')" width="160">
                  <template #default="scope">
                    {{ formatTime(scope.row.lastUpdateTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50]"
                :total="filteredAvailableMachines.length"
                layout="total, sizes, prev, pager, next"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>

          <!-- 右侧：已发布机器 -->
          <div class="right-panel">
            <div class="panel-header">
              <h4>{{ $t('ticket.detail.publishedMachines') }} ({{ publishedMachines.length }})</h4>
              <div class="header-actions">
                <el-button type="info" size="small" @click="refreshMachines" :loading="loading">
                  <el-icon><Refresh /></el-icon>
                  {{ $t('common.refresh') }}
                </el-button>
                <el-button type="warning" size="small" @click="rollbackSelected" :disabled="selectedPublishedMachines.length === 0">
                  {{ $t('ticket.detail.rollbackSelected') }}
                </el-button>
              </div>
            </div>

            <!-- 筛选器 -->
            <div class="filter-bar">
              <el-form :model="publishedFilterForm" inline size="small">
                <el-form-item :label="$t('ticket.detail.ip')">
                  <el-input v-model="publishedFilterForm.ip" :placeholder="$t('ticket.detail.searchIp')" clearable style="width: 120px;" />
                </el-form-item>
                <el-form-item :label="$t('ticket.detail.status')">
                  <el-select v-model="publishedFilterForm.status" :placeholder="$t('ticket.detail.status')" clearable style="width: 100px;">
                    <el-option label="Running" value="Running" />
                    <el-option label="Success" value="Success" />
                    <el-option label="NoGray" value="NoGray" />
                    <el-option label="Offline" value="Offline" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handlePublishedFilter">{{ $t('common.filter') }}</el-button>
                  <el-button @click="resetPublishedFilter">{{ $t('common.reset') }}</el-button>
                </el-form-item>
              </el-form>
            </div>

            <!-- 已发布机器列表 -->
            <div class="machine-table">
              <el-table 
                :data="filteredPublishedMachines" 
                v-loading="loading"
                @selection-change="handlePublishedSelectionChange"
                style="width: 100%"
                height="300"
                stripe
                border
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="ip" :label="$t('ticket.detail.ip')" width="140">
                  <template #default="scope">
                    <div class="ip-cell">
                      <span>{{ scope.row.ip }}</span>
                      <el-icon class="copy-icon" @click="copyToClipboard(scope.row.ip)">
                        <CopyDocument />
                      </el-icon>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="status" :label="$t('ticket.detail.status')" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="version" :label="$t('ticket.detail.currentVersion')" width="120" />
                <el-table-column prop="lastUpdateTime" :label="$t('ticket.detail.lastUpdate')" width="160">
                  <template #default="scope">
                    {{ formatTime(scope.row.lastUpdateTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="publishedCurrentPage"
                v-model:page-size="publishedPageSize"
                :page-sizes="[10, 20, 50]"
                :total="filteredPublishedMachines.length"
                layout="total, sizes, prev, pager, next"
                @size-change="handlePublishedSizeChange"
                @current-change="handlePublishedCurrentChange"
              />
            </div>
          </div>
        </div>
        
        <!-- 底部操作按钮 -->
        <div class="dialog-footer" style="margin-top: 20px; text-align: center;">
          <el-button 
            v-if="allMachinesSuccess" 
            type="success" 
            size="large" 
            @click="completeGrayPublish"
            :loading="publishing"
          >
            <el-icon><Check /></el-icon>
            {{ $t('ticket.detail.complete') }}
          </el-button>
          <el-button @click="grayPublishDialogVisible = false" size="large">
            {{ $t('common.cancel') }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, CopyDocument, Check } from '@element-plus/icons-vue'
import { getTicketById, updateTicket } from '@/api/ticket'
import {getSubscribedContainers, publishConfig, rollbackConfig} from '@/api/config'
import type { Ticket } from '@/types/ticket'
import type { MachineInstance } from '@/types/machine'
import { TicketPhase } from '@/types/ticket'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const ticketDetail = ref<Ticket | null>(null)
const loading = ref(false)
const isEncrypted = ref(false)

// 弹窗相关数据
const grayPublishDialogVisible = ref(false)
const publishing = ref(false)

// 机器列表相关数据
const availableMachines = ref<MachineInstance[]>([])
const publishedMachines = ref<MachineInstance[]>([])
const selectedMachines = ref<MachineInstance[]>([])
const selectedPublishedMachines = ref<MachineInstance[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const publishedCurrentPage = ref(1)
const publishedPageSize = ref(10)

// 筛选表单
const filterForm = ref({
  ip: '',
  status: ''
})

const publishedFilterForm = ref({
  ip: '',
  status: ''
})

// 计算属性
const ticketId = computed(() => route.params.id as string)

// 检查是否所有机器都是Success状态
const allMachinesSuccess = computed(() => {
  const allMachines = [...availableMachines.value, ...publishedMachines.value]
  return allMachines.length > 0 && allMachines.every(machine => machine.status === 'Success')
})

// 筛选后的机器列表
const filteredAvailableMachines = computed(() => {
  let machines = availableMachines.value
  
  if (filterForm.value.ip) {
    machines = machines.filter(machine => machine.ip.includes(filterForm.value.ip))
  }
  
  if (filterForm.value.status) {
    machines = machines.filter(machine => machine.status === filterForm.value.status)
  }
  
  return machines
})

const filteredPublishedMachines = computed(() => {
  let machines = publishedMachines.value
  
  if (publishedFilterForm.value.ip) {
    machines = machines.filter(machine => machine.ip.includes(publishedFilterForm.value.ip))
  }
  
  if (publishedFilterForm.value.status) {
    machines = machines.filter(machine => machine.status === publishedFilterForm.value.status)
  }
  
  return machines
})

// 状态步骤定义
const statusSteps = computed(() => [
  {
    title: t('ticket.detail.steps.submit'),
    phase: TicketPhase.Submit
  },
  {
    title: t('ticket.detail.steps.reviewing'),
    phase: TicketPhase.Reviewing
  },
  {
    title: t('ticket.detail.steps.grayPublish'),
    phase: TicketPhase.GrayPublish
  },
  {
    title: t('ticket.detail.steps.success'),
    phase: TicketPhase.Success
  },
  {
    title: t('ticket.phases.rejected'),
    phase: TicketPhase.Rejected
  },
  {
    title: t('ticket.phases.cancelled'),
    phase: TicketPhase.Cancelled
  }
])

// 模拟操作历史数据
const operationHistory = ref([
  {
    time: '2024-01-15 10:30:00',
    type: 'primary',
    title: '工单创建',
    description: '用户创建了工单申请',
    operator: 'user1'
  },
  {
    time: '2024-01-15 14:20:00',
    type: 'warning',
    title: '工单处理中',
    description: '管理员开始处理工单',
    operator: 'admin'
  },
  {
    time: '2024-01-16 09:15:00',
    type: 'success',
    title: '工单审批通过',
    description: '工单已审批通过，配置已更新',
    operator: 'admin'
  }
])

// 方法
const loadTicketDetail = async () => {
  if (!ticketId.value) {
    ElMessage.error(t('ticket.detail.messages.ticketIdRequired'))
    return
  }

  loading.value = true
  try {
    const response = await getTicketById(Number(ticketId.value))
    if (response.success) {
      ticketDetail.value = response.data
      console.log('加载的工单详情:', ticketDetail.value)
      console.log('工单状态:', ticketDetail.value?.phase)
      console.log('状态类型:', typeof ticketDetail.value?.phase)
    } else {
      ElMessage.error(response.message || t('ticket.detail.messages.loadFailed'))
    }
  } catch (error) {
    console.error(t('ticket.detail.messages.loadFailed'), error)
    ElMessage.error(t('ticket.detail.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const refreshData = () => {
  loadTicketDetail()
}

const getPhaseTagType = (phase?: string) => {
  switch (phase) {
    case TicketPhase.Submit:
      return 'info'
    case TicketPhase.Reviewing:
      return 'warning'
    case TicketPhase.Success:
      return 'success'
    case TicketPhase.Rejected:
      return 'danger'
    case TicketPhase.GrayPublish:
      return 'warning'
    case TicketPhase.Cancelled:
      return 'info'
    default:
      return 'info'
  }
}

const getPhaseText = (phase?: string) => {
  switch (phase) {
    case TicketPhase.Submit:
      return t('ticket.phases.submit')
    case TicketPhase.Reviewing:
      return t('ticket.phases.reviewing')
    case TicketPhase.Success:
      return t('ticket.phases.success')
    case TicketPhase.Rejected:
      return t('ticket.phases.rejected')
    case TicketPhase.GrayPublish:
      return t('ticket.phases.grayPublish')
    case TicketPhase.Cancelled:
      return t('ticket.phases.cancelled')
    default:
      return phase || ''
  }
}

const getOperationType = (type: string) => {
  switch (type) {
    case 'primary':
      return 'primary'
    case 'warning':
      return 'warning'
    case 'success':
      return 'success'
    case 'danger':
      return 'danger'
    default:
      return 'info'
  }
}

const copyOldData = () => {
  if (ticketDetail.value?.oldData) {
    navigator.clipboard.writeText(ticketDetail.value.oldData)
    ElMessage.success(t('common.copySuccess'))
  }
}

const copyNewData = () => {
  if (ticketDetail.value?.newData) {
    navigator.clipboard.writeText(ticketDetail.value.newData)
    ElMessage.success(t('common.copySuccess'))
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 状态进度条相关方法
const getCurrentStepIndex = () => {
  if (!ticketDetail.value?.phase) return 0
  
  const currentPhase = ticketDetail.value.phase
  const currentSteps = getCurrentSteps()
  const normalizedPhase = currentPhase.toUpperCase()
  const stepIndex = currentSteps.findIndex(step => step.phase === normalizedPhase)
  return stepIndex >= 0 ? stepIndex : currentSteps.length - 1
}

const getStepStatus = (index: number) => {
  if (!ticketDetail.value?.phase) return 'wait'
  
  const currentPhase = ticketDetail.value.phase
  const currentSteps = getCurrentSteps()
  const normalizedPhase = currentPhase.toUpperCase()
  const currentStepIndex = currentSteps.findIndex(step => step.phase === normalizedPhase)
  
  if (index < currentStepIndex) {
    return 'finish'
  } else if (index === currentStepIndex) {
    return 'process'
  } else {
    return 'wait'
  }
}

const getCurrentSteps = () => {
  const currentPhase = ticketDetail.value?.phase
  console.log('Current phase:', currentPhase)
  console.log('Status steps:', statusSteps.value)
  
  if (!currentPhase) {
    console.log('No current phase, returning empty array')
    return []
  }

  // 特殊处理GrayPublish状态
  if (currentPhase === 'GrayPublish') {
    console.log('Found GrayPublish, returning first 4 steps')
    return statusSteps.value.slice(0, 4)
  }

  // 处理其他状态的大小写不匹配问题
  const normalizedPhase = currentPhase.toUpperCase()
  console.log('Normalized phase:', normalizedPhase)
  
  const currentStepIndex = statusSteps.value.findIndex(step => {
    console.log('Comparing step.phase:', step.phase, 'with normalizedPhase:', normalizedPhase)
    return step.phase === normalizedPhase
  })
  console.log('Current step index:', currentStepIndex)
  
  const result = statusSteps.value.slice(0, currentStepIndex + 1)
  console.log('Current steps to show:', result)
  return result
}

// Action按钮相关方法
const getActionButtonType = (action: string) => {
  switch (action) {
    case 'Approve':
      return 'success'
    case 'Reject':
      return 'danger'
    case 'Cancel':
      return 'warning'
    case 'Complete':
      return 'primary'
    default:
      return 'primary'
  }
}

const getActionIcon = (action: string) => {
  switch (action) {
    case 'Approve':
      return 'Check'
    case 'Reject':
      return 'Close'
    case 'Cancel':
      return 'Close'
    case 'Complete':
      return 'Select'
    default:
      return 'Operation'
  }
}

const getActionText = (action: string) => {
  switch (action) {
    case 'Approve':
      return t('ticket.actions.approve')
    case 'Reject':
      return t('ticket.actions.reject')
    case 'Cancel':
      return t('ticket.actions.cancel')
    case 'Complete':
      return t('ticket.actions.complete')
    case 'Publish':
      return t('ticket.actions.publish')
    default:
      return action
  }
}

const handleAction = async (action: string) => {
  try {
    // 特殊处理Publish操作
    if (action === 'Publish') {
      // 显示灰度发布弹窗
      grayPublishDialogVisible.value = true
      // 加载机器列表
      await loadMachineList()
      return
    }

    const actionText = getActionText(action)
    const confirmMessage = t('ticket.actions.confirmMessage', { action: actionText })
    
    await ElMessageBox.confirm(confirmMessage, t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    if (!ticketDetail.value?.id) {
      ElMessage.error(t('ticket.detail.messages.ticketIdRequired'))
      return
    }
    
    // 根据action确定新的action
    let actionValue: string
    switch (action) {
      case 'Approve':
        actionValue = 'Approve'
        break
      case 'Reject':
        actionValue = 'Reject'
        break
      case 'Cancel':
        actionValue = 'Cancel'
        break
      case 'Complete':
        actionValue = 'Complete'
        break
      default:
        ElMessage.error('不支持的操作')
        return
    }
    
    // 调用后端API执行操作
    const response = await updateTicket(ticketDetail.value.id, {
      action: actionValue,
      operator: 'system' // 或者从用户信息中获取
    })
    
    if (response.success) {
      ElMessage.success(t('ticket.actions.successMessage', { action: actionText }))
      // 刷新数据
      await loadTicketDetail()
    } else {
      ElMessage.error(response.message || t('ticket.actions.errorMessage'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(t('ticket.actions.errorMessage'))
    }
  }
}

// 机器列表相关方法
const loadMachineList = async () => {
  loading.value = true
  try {
    // 调用API获取机器列表
    if (ticketDetail.value?.dataId && ticketDetail.value?.id) {
      const response = await getSubscribedContainers(ticketDetail.value.dataId, ticketDetail.value.id)
      if (response.success) {
        const allMachines = response.data
        
        // 自动分类机器：状态为"Success"的机器显示在右边，其他显示在左边
        availableMachines.value = allMachines.filter(machine => machine.status !== 'Success')
        publishedMachines.value = allMachines.filter(machine => machine.status === 'Success')
        
        console.log('机器分类完成:', {
          available: availableMachines.value.length,
          published: publishedMachines.value.length,
          total: allMachines.length
        })
      } else {
        ElMessage.error(response.message || '获取机器列表失败')
      }
    }
  } catch (error) {
    console.error('获取机器列表失败:', error)
    ElMessage.error('获取机器列表失败')
  } finally {
    loading.value = false
  }
}

const refreshMachines = async () => {
  await loadMachineList()
}

// 确认灰度发布
const confirmGrayPublish = async () => {
  publishing.value = true
  try {
    if (!ticketDetail.value?.id) {
      ElMessage.error(t('ticket.detail.messages.ticketIdRequired'))
      return
    }

    // 调用后端API执行灰度发布
    const response = await updateTicket(ticketDetail.value.id, {
      action: 'Publish',
      operator: 'system' // 或者从用户信息中获取
    })
    
    if (response.success) {
      ElMessage.success('灰度发布成功')
      grayPublishDialogVisible.value = false
      // 刷新数据
      await loadTicketDetail()
    } else {
      ElMessage.error(response.message || '灰度发布失败')
    }
  } catch (error) {
    console.error('灰度发布失败:', error)
    ElMessage.error('灰度发布失败')
  } finally {
    publishing.value = false
  }
}

const handleSelectionChange = (selection: MachineInstance[]) => {
  selectedMachines.value = selection
}

const handlePublishedSelectionChange = (selection: MachineInstance[]) => {
  selectedPublishedMachines.value = selection
}

const handleFilter = () => {
  // 筛选逻辑已在computed中处理
}

const resetFilter = () => {
  filterForm.value = { ip: '', status: '' }
}

const handlePublishedFilter = () => {
  // 筛选逻辑已在computed中处理
}

const resetPublishedFilter = () => {
  publishedFilterForm.value = { ip: '', status: '' }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

const handlePublishedSizeChange = (size: number) => {
  publishedPageSize.value = size
}

const handlePublishedCurrentChange = (page: number) => {
  publishedCurrentPage.value = page
}

// 发布到选中的机器
const publishToSelected = async () => {
  if (selectedMachines.value.length === 0) {
    ElMessage.warning(t('ticket.detail.machineList.selectMachinesFirst'))
    return
  }

  try {
    publishing.value = true
    
    // 调用服务端publishConfig接口
    const response = await publishConfig(ticketDetail.value.dataId, selectedMachines.value.map(machine => machine.ip), ticketDetail.value.id)
    
    if (response.success) {
      ElMessage.success(t('ticket.detail.grayPublishDialog.publishSuccess'))
      // 关闭弹窗
      grayPublishDialogVisible.value = false
      // 刷新ticket详情
      await loadTicketDetail()
    } else {
      ElMessage.error(response.message || t('ticket.detail.grayPublishDialog.publishFailed'))
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(t('ticket.detail.grayPublishDialog.publishFailed'))
  } finally {
    publishing.value = false
  }
}

const publishToAll = async () => {
  // 过滤出状态为NoGray的机器
  const noGrayMachines = availableMachines.value.filter(machine => machine.status === 'NoGray')
  
  if (noGrayMachines.length === 0) {
    ElMessage.warning('没有状态为NoGray的机器可发布')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要发布到全部 ${noGrayMachines.length} 台NoGray状态的机器吗？`, '确认发布', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用发布API
    const selectedIPs = noGrayMachines.map(machine => machine.ip)
    console.log('发布到所有NoGray机器，IP列表:', selectedIPs)
    const response = await publishConfig(ticketDetail.value.dataId, selectedIPs, ticketDetail.value.id)
    
    if (response.success) {
      ElMessage.success(t('ticket.detail.grayPublishDialog.publishSuccess'))
      await refreshMachines()
    } else {
      ElMessage.error(response.message || t('ticket.detail.grayPublishDialog.publishFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const rollbackSelected = async () => {
  console.log('回滚操作开始，选中的已发布机器:', selectedPublishedMachines.value)
  
  if (selectedPublishedMachines.value.length === 0) {
    ElMessage.warning('请选择要回滚的机器')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要回滚选中的 ${selectedPublishedMachines.value.length} 台机器吗？`, '确认回滚', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用服务端publishConfig接口
    const selectedIPs = selectedPublishedMachines.value.map(machine => machine.ip)
    console.log('回滚选中的IP:', selectedIPs)
    const response = await rollbackConfig(ticketDetail.value.dataId, selectedIPs, ticketDetail.value.id)

    if (response.success) {
      ElMessage.success(t('ticket.detail.grayPublishDialog.publishSuccess'))
      // 关闭弹窗
      grayPublishDialogVisible.value = false
      // 刷新ticket详情
      await loadTicketDetail()
    } else {
      ElMessage.error(response.message || t('ticket.detail.grayPublishDialog.publishFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回滚失败')
    }
  }
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('复制成功')
}

const showMachineDetail = (machine: MachineInstance) => {
  ElMessage.info(`查看机器详情: ${machine.ip}`)
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'Running':
      return 'success'
    case 'Success':
      return 'success'
    case 'NoGray':
      return 'warning'
    case 'Offline':
      return 'danger'
    default:
      return 'info'
  }
}

// 生命周期
onMounted(() => {
  loadTicketDetail()
  refreshMachines()
  
  // 添加定时器，每10秒检查一次机器状态
  const statusCheckInterval = setInterval(async () => {
    if (grayPublishDialogVisible.value) {
      await refreshMachines()
    }
  }, 10000)
  
  // 组件卸载时清理定时器
  onUnmounted(() => {
    clearInterval(statusCheckInterval)
  })
})

const completeGrayPublish = async () => {
  if (!ticketDetail.value?.id) {
    ElMessage.error(t('ticket.detail.messages.ticketIdRequired'))
    return
  }

  try {
    await ElMessageBox.confirm(`确定要完成灰度发布吗？`, '确认完成', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用后端API，将工单状态更新为Success
    const response = await updateTicket(ticketDetail.value.id, {
      action: 'Complete',
      operator: 'system' // 或者从用户信息中获取
    })

    if (response.success) {
      ElMessage.success('灰度发布完成')
      grayPublishDialogVisible.value = false
      await loadTicketDetail()
    } else {
      ElMessage.error(response.message || '灰度发布完成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('灰度发布完成失败:', error)
      ElMessage.error('灰度发布完成失败')
    }
  }
}
</script>

<style scoped lang="scss">
.ticket-detail {
  padding: 20px;
}

.detail-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-size: 18px;
      font-weight: 600;
    }
    
    .actions {
      display: flex;
      gap: 10px;
      align-items: center;
      
      .action-buttons {
        display: flex;
        gap: 8px;
        margin-right: 10px;
      }
    }
  }
}

.basic-info,
.data-comparison,
.operation-history,
.machine-list {
  margin-bottom: 30px;
  
  h3 {
    margin-bottom: 20px;
    color: #303133;
    font-size: 16px;
    font-weight: 600;
  }
}

.machine-list {
  .machine-management {
    display: flex;
    gap: 20px;
    
    .left-panel,
    .right-panel {
      flex: 1;
      
      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        
        h4 {
          margin: 0;
          color: #303133;
          font-size: 14px;
          font-weight: 600;
        }
        
        .header-actions {
          display: flex;
          gap: 8px;
        }
      }
      
      .filter-bar {
        margin-bottom: 15px;
        padding: 10px;
        background: #f8f9fa;
        border-radius: 4px;
      }
      
      .machine-table {
        margin-bottom: 15px;
        
        .ip-cell {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .copy-icon {
            cursor: pointer;
            color: #909399;
            
            &:hover {
              color: #409eff;
            }
          }
        }
      }
      
      .pagination {
        display: flex;
        justify-content: center;
        margin-top: 15px;
      }
    }
  }
}

.status-progress {
  margin-bottom: 30px;
  
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      color: #303133;
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 0;
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
    }
  }
  
  .progress-container {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .el-steps {
      .el-step {
        .el-step__title {
          font-weight: 600;
        }
        
        .el-step__description {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

.data-card {
  .data-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .data-content {
    .no-data {
      text-align: center;
      color: #909399;
      font-style: italic;
      padding: 20px;
    }
  }
}

.operation-history {
  .el-timeline-item {
    .operator {
      color: #909399;
      font-size: 12px;
      margin-top: 5px;
    }
  }
}

// 灰度发布弹窗样式
.gray-publish-dialog {
  .el-dialog {
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
    overflow: hidden;
    
    .el-dialog__header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-bottom: none;
      border-radius: 12px 12px 0 0;
      padding: 16px 20px;
      
      .el-dialog__title {
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;
      }
      
      .el-dialog__headerbtn {
        .el-dialog__close {
          color: #ffffff;
          font-size: 16px;
          
          &:hover {
            color: #f0f0f0;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 16px 20px;
      max-height: 80vh;
      overflow-y: auto;
      background: #fafbfc;
    }
    
    .el-dialog__footer {
      background: #ffffff;
      border-top: 1px solid #e8eaed;
      border-radius: 0 0 12px 12px;
      padding: 16px 20px;
      box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
    }
  }
  
  .gray-publish-content {
    .success-indicator {
      margin-bottom: 20px;
      padding: 15px;
      background-color: #f0f9ff;
      border: 1px solid #409eff;
      border-radius: 4px;
    }

    .machine-management {
      display: flex;
      gap: 16px;
      height: 100%;
      
      .left-panel,
      .right-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #ffffff;
        border: 1px solid #e1e5e9;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
          transform: translateY(-1px);
        }
        
        .panel-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 16px;
          background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
          border-bottom: 1px solid #e1e5e9;
          position: relative;
          
          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
          }
          
          h4 {
            margin: 0;
            color: #2c3e50;
            font-size: 14px;
            font-weight: 600;
            display: flex;
            align-items: center;
            
            &::before {
              content: '';
              width: 3px;
              height: 14px;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              border-radius: 2px;
              margin-right: 8px;
            }
          }
          
          .header-actions {
            display: flex;
            gap: 6px;
            
            .el-button {
              font-size: 11px;
              padding: 6px 12px;
              border-radius: 4px;
              font-weight: 500;
              transition: all 0.3s ease;
              
              &:hover {
                transform: translateY(-1px);
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
              }
            }
          }
        }
        
        .filter-bar {
          padding: 10px 16px;
          background: #f8f9fa;
          border-bottom: 1px solid #e1e5e9;
          
          .el-form {
            margin: 0;
            
            .el-form-item {
              margin-bottom: 0;
              margin-right: 12px;
              
              .el-form-item__label {
                font-size: 12px;
                color: #495057;
                font-weight: 500;
              }
              
              .el-input,
              .el-select {
                .el-input__inner {
                  border-radius: 4px;
                  border: 1px solid #d1d5db;
                  transition: all 0.3s ease;
                  height: 32px;
                  font-size: 12px;
                  
                  &:focus {
                    border-color: #667eea;
                    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
                  }
                }
              }
            }
            
            .el-button {
              border-radius: 4px;
              font-weight: 500;
              transition: all 0.3s ease;
              height: 32px;
              font-size: 12px;
              padding: 0 12px;
              
              &:hover {
                transform: translateY(-1px);
              }
            }
          }
        }
        
        .machine-table {
          flex: 1;
          padding: 0;
          
          .el-table {
            border: none;
            
            .el-table__header {
              background: #f8f9fa;
              
              th {
                background: #f8f9fa;
                color: #495057;
                font-weight: 600;
                font-size: 12px;
                border-bottom: 1px solid #e1e5e9;
                padding: 8px 0;
              }
            }
            
            .el-table__body {
              .el-table__row {
                transition: all 0.3s ease;
                
                &:hover {
                  background-color: #f8f9fa;
                  transform: scale(1.005);
                }
                
                td {
                  padding: 8px 0;
                  border-bottom: 1px solid #f1f3f4;
                  font-size: 12px;
                }
              }
            }
          }
          
          .ip-cell {
            display: flex;
            align-items: center;
            gap: 6px;
            
            .copy-icon {
              cursor: pointer;
              color: #6c757d;
              font-size: 12px;
              transition: all 0.3s ease;
              
              &:hover {
                color: #667eea;
                transform: scale(1.1);
              }
            }
          }
        }
        
        .pagination {
          padding: 10px 16px;
          background: #f8f9fa;
          border-top: 1px solid #e1e5e9;
          display: flex;
          justify-content: center;
          
          .el-pagination {
            .el-pagination__total {
              font-size: 12px;
              color: #6c757d;
              font-weight: 500;
            }
            
            .el-pagination__sizes {
              .el-select {
                .el-input {
                  .el-input__inner {
                    font-size: 12px;
                    border-radius: 4px;
                    height: 28px;
                  }
                }
              }
            }
            
            .btn-prev,
            .btn-next {
              border-radius: 4px;
              transition: all 0.3s ease;
              height: 28px;
              width: 28px;
              
              &:hover {
                transform: translateY(-1px);
              }
            }
            
            .el-pager {
              li {
                border-radius: 4px;
                transition: all 0.3s ease;
                height: 28px;
                width: 28px;
                line-height: 28px;
                
                &:hover {
                  transform: translateY(-1px);
                }
              }
            }
          }
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  
  .el-button {
    padding: 8px 20px;
    font-size: 13px;
    font-weight: 500;
    border-radius: 6px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
    }
    
    &.el-button--primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      
      &:hover {
        background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
      }
    }
    
    &.el-button--default {
      border: 1px solid #d1d5db;
      color: #495057;
      
      &:hover {
        border-color: #667eea;
        color: #667eea;
      }
    }
  }
}

// 状态标签样式优化
.el-tag {
  border-radius: 6px;
  font-weight: 500;
  font-size: 12px;
  padding: 4px 8px;
  transition: all 0.3s ease;
  
  &.el-tag--success {
    background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
    border: none;
    color: #ffffff;
  }
  
  &.el-tag--warning {
    background: linear-gradient(135deg, #ffc107 0%, #fd7e14 100%);
    border: none;
    color: #ffffff;
  }
  
  &.el-tag--danger {
    background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%);
    border: none;
    color: #ffffff;
  }
  
  &.el-tag--info {
    background: linear-gradient(135deg, #17a2b8 0%, #6f42c1 100%);
    border: none;
    color: #ffffff;
  }
}

// 表格选择框样式优化
.el-table {
  .el-table__header {
    .el-checkbox {
      .el-checkbox__input {
        .el-checkbox__inner {
          border-radius: 4px;
          border: 2px solid #d1d5db;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: #667eea;
          }
        }
      }
    }
  }
  
  .el-table__body {
    .el-checkbox {
      .el-checkbox__input {
        .el-checkbox__inner {
          border-radius: 4px;
          border: 2px solid #d1d5db;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: #667eea;
          }
        }
      }
    }
  }
}

// 滚动条样式优化
.gray-publish-dialog {
  .el-dialog__body {
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f3f4;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 4px;
      
      &:hover {
        background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
      }
    }
  }
}

// 加载状态优化
.el-loading-mask {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  
  .el-loading-spinner {
    .circular {
      width: 40px;
      height: 40px;
      
      .path {
        stroke: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
    }
  }
}
</style> 