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
              {{ getPhaseText(ticketDetail?.phase) }}
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
            {{ formatTime(ticketDetail?.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.updateTime')">
            {{ formatTime(ticketDetail?.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.createBy')">
            {{ ticketDetail?.createBy || $t('common.none') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ticket.updateBy')">
            {{ ticketDetail?.updateBy || $t('common.none') }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketById } from '@/api/ticket'
import type { Ticket } from '@/types/ticket'
import { TicketPhase } from '@/types/ticket'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const ticketDetail = ref<Ticket | null>(null)
const loading = ref(false)
const isEncrypted = ref(false)

// 计算属性
const ticketId = computed(() => route.params.id as string)

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
    case TicketPhase.PENDING:
      return 'info'
    case TicketPhase.PROCESSING:
      return 'warning'
    case TicketPhase.APPROVED:
      return 'success'
    case TicketPhase.REJECTED:
      return 'danger'
    case TicketPhase.COMPLETED:
      return 'success'
    default:
      return ''
  }
}

const getPhaseText = (phase?: string) => {
  switch (phase) {
    case TicketPhase.PENDING:
      return t('ticket.phases.pending')
    case TicketPhase.PROCESSING:
      return t('ticket.phases.processing')
    case TicketPhase.APPROVED:
      return t('ticket.phases.approved')
    case TicketPhase.REJECTED:
      return t('ticket.phases.rejected')
    case TicketPhase.COMPLETED:
      return t('ticket.phases.completed')
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

// 生命周期
onMounted(() => {
  loadTicketDetail()
})
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
    }
  }
}

.basic-info,
.data-comparison,
.operation-history {
  margin-bottom: 30px;
  
  h3 {
    margin-bottom: 20px;
    color: #303133;
    font-size: 16px;
    font-weight: 600;
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
</style> 