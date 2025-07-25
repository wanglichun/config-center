<template>
  <div class="my-tickets">
    <!-- 搜索和过滤 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item :label="t('ticket.ticketTitle')">
          <el-input
            v-model="queryForm.title"
            :placeholder="t('ticket.placeholders.search')"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item :label="t('ticket.type')">
          <el-select v-model="queryForm.type" clearable :placeholder="t('ticket.placeholders.type')">
            <el-option
              v-for="type in ticketTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.priority')">
          <el-select v-model="queryForm.priority" clearable :placeholder="t('ticket.placeholders.priority')">
            <el-option
              v-for="priority in ticketPriorities"
              :key="priority.value"
              :label="priority.label"
              :value="priority.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.status')">
          <el-select v-model="queryForm.status" clearable>
            <el-option
              v-for="status in ticketStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            {{ t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ t('common.reset') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工单列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('ticket.myTickets') }}</span>
          <el-button @click="loadData">
            <el-icon><Refresh /></el-icon>
            {{ t('common.refresh') }}
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="ticketList"
        style="width: 100%"
      >
        <el-table-column prop="id" :label="t('ticket.ticketId')" width="80" />
        <el-table-column prop="title" :label="t('ticket.ticketTitle')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" :label="t('ticket.type')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ t(`ticket.types.${row.type}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" :label="t('ticket.priority')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ t(`ticket.priorities.${row.priority}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('ticket.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ t(`ticket.statuses.${row.status}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" :label="t('ticket.reporter')" width="120" />
        <el-table-column prop="createTime" :label="t('ticket.createTime')" width="160" />
        <el-table-column prop="dueTime" :label="t('ticket.dueTime')" width="160">
          <template #default="{ row }">
            <span v-if="row.dueTime" :class="{ 'overdue': isOverdue(row.dueTime, row.status) }">
              {{ row.dueTime }}
            </span>
            <span v-else>{{ t('common.none') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">
              {{ t('ticket.actions.view') }}
            </el-button>
            <el-button size="small" type="primary" @click="handleUpdateStatus(row)">
              {{ t('ticket.actions.updateStatus') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 更新状态对话框 -->
    <el-dialog
      v-model="showStatusDialog"
      :title="t('ticket.actions.updateStatus')"
      width="400px"
    >
      <el-form :model="statusForm" label-width="80px">
        <el-form-item :label="t('ticket.status')">
          <el-select v-model="statusForm.status" style="width: 100%">
            <el-option
              v-for="status in ticketStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStatusDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleStatusSubmit" :loading="submitting">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getTicketList, updateTicketStatus } from '@/api/ticket'
import type { Ticket, TicketQuery } from '@/types/ticket'
import { TicketType, TicketPriority, TicketStatus } from '@/types/ticket'

const { t } = useI18n()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const ticketList = ref<Ticket[]>([])
const total = ref(0)
const showStatusDialog = ref(false)
const currentTicket = ref<Ticket | null>(null)

// 查询表单
const queryForm = reactive<TicketQuery>({
  pageNum: 1,
  pageSize: 20,
  title: '',
  type: undefined,
  priority: undefined,
  status: undefined,
  assignee: 'current_user', // 只查询分配给当前用户的工单
  reporter: '',
  startTime: '',
  endTime: '',
  tags: []
})

// 状态表单
const statusForm = reactive({
  status: TicketStatus.PENDING
})

// 计算属性
const ticketTypes = computed(() => [
  { value: TicketType.BUG, label: t('ticket.types.BUG') },
  { value: TicketType.FEATURE, label: t('ticket.types.FEATURE') },
  { value: TicketType.CONFIG, label: t('ticket.types.CONFIG') },
  { value: TicketType.DEPLOY, label: t('ticket.types.DEPLOY') },
  { value: TicketType.OTHER, label: t('ticket.types.OTHER') }
])

const ticketPriorities = computed(() => [
  { value: TicketPriority.LOW, label: t('ticket.priorities.LOW') },
  { value: TicketPriority.MEDIUM, label: t('ticket.priorities.MEDIUM') },
  { value: TicketPriority.HIGH, label: t('ticket.priorities.HIGH') },
  { value: TicketPriority.URGENT, label: t('ticket.priorities.URGENT') }
])

const ticketStatuses = computed(() => [
  { value: TicketStatus.PENDING, label: t('ticket.statuses.PENDING') },
  { value: TicketStatus.IN_PROGRESS, label: t('ticket.statuses.IN_PROGRESS') },
  { value: TicketStatus.RESOLVED, label: t('ticket.statuses.RESOLVED') },
  { value: TicketStatus.CLOSED, label: t('ticket.statuses.CLOSED') },
  { value: TicketStatus.REJECTED, label: t('ticket.statuses.REJECTED') }
])

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const result = await getTicketList(queryForm)
    ticketList.value = result.records
    total.value = result.total
  } catch (error) {
    ElMessage.error(t('ticket.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  Object.assign(queryForm, {
    pageNum: 1,
    pageSize: 20,
    title: '',
    type: undefined,
    priority: undefined,
    status: undefined,
    assignee: 'current_user',
    reporter: '',
    startTime: '',
    endTime: '',
    tags: []
  })
  loadData()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadData()
}

const handleCurrentChange = (page: number) => {
  queryForm.pageNum = page
  loadData()
}

const handleView = (row: Ticket) => {
  router.push(`/ticket/detail/${row.id}`)
}

const handleUpdateStatus = (row: Ticket) => {
  currentTicket.value = row
  statusForm.status = row.status
  showStatusDialog.value = true
}

const handleStatusSubmit = async () => {
  if (!currentTicket.value) return

  submitting.value = true
  try {
    await updateTicketStatus(currentTicket.value.id, statusForm.status)
    ElMessage.success(t('ticket.messages.statusUpdateSuccess'))
    showStatusDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('状态更新失败')
  } finally {
    submitting.value = false
  }
}

// 工具方法
const getTypeTagType = (type: TicketType) => {
  const typeMap: Record<TicketType, string> = {
    [TicketType.BUG]: 'danger',
    [TicketType.FEATURE]: 'success',
    [TicketType.CONFIG]: 'warning',
    [TicketType.DEPLOY]: 'info',
    [TicketType.OTHER]: ''
  }
  return typeMap[type]
}

const getPriorityTagType = (priority: TicketPriority) => {
  const priorityMap: Record<TicketPriority, string> = {
    [TicketPriority.LOW]: 'info',
    [TicketPriority.MEDIUM]: 'warning',
    [TicketPriority.HIGH]: 'danger',
    [TicketPriority.URGENT]: 'danger'
  }
  return priorityMap[priority]
}

const getStatusTagType = (status: TicketStatus) => {
  const statusMap: Record<TicketStatus, string> = {
    [TicketStatus.PENDING]: 'warning',
    [TicketStatus.IN_PROGRESS]: 'primary',
    [TicketStatus.RESOLVED]: 'success',
    [TicketStatus.CLOSED]: 'info',
    [TicketStatus.REJECTED]: 'danger'
  }
  return statusMap[status]
}

const isOverdue = (dueTime: string, status: TicketStatus) => {
  if (status === TicketStatus.CLOSED) return false
  return new Date(dueTime) < new Date()
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-tickets {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.overdue {
  color: #f56c6c;
  font-weight: bold;
}
</style> 