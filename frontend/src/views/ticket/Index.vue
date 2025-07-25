<template>
  <div class="ticket-index">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="3" v-for="stat in statsData" :key="stat.key">
        <el-card class="stats-card" shadow="hover">
          <div class="stats-content">
            <div class="stats-number">{{ stat.value }}</div>
            <div class="stats-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ t('ticket.add') }}
          </el-button>
          <el-button 
            type="danger" 
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            {{ t('common.delete') }}
          </el-button>
        </div>
        <div class="right">
          <el-button @click="loadData">
            <el-icon><Refresh /></el-icon>
            {{ t('common.refresh') }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 工单列表 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="ticketList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
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
        <el-table-column prop="assignee" :label="t('ticket.assignee')" width="120" />
        <el-table-column prop="reporter" :label="t('ticket.reporter')" width="120" />
        <el-table-column prop="createTime" :label="t('ticket.createTime')" width="160" />
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">
              {{ t('ticket.actions.view') }}
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              {{ t('ticket.actions.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              {{ t('ticket.actions.delete') }}
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

    <!-- 新建/编辑工单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('ticket.edit') : t('ticket.add')"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item :label="t('ticket.ticketTitle')" prop="title">
          <el-input v-model="form.title" :placeholder="t('ticket.placeholders.title')" />
        </el-form-item>
        <el-form-item :label="t('ticket.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            :placeholder="t('ticket.placeholders.description')"
          />
        </el-form-item>
        <el-form-item :label="t('ticket.type')" prop="type">
          <el-select v-model="form.type" :placeholder="t('ticket.placeholders.type')" style="width: 100%">
            <el-option
              v-for="type in ticketTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.priority')" prop="priority">
          <el-select v-model="form.priority" :placeholder="t('ticket.placeholders.priority')" style="width: 100%">
            <el-option
              v-for="priority in ticketPriorities"
              :key="priority.value"
              :label="priority.label"
              :value="priority.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.assignee')" prop="assignee">
          <el-select v-model="form.assignee" :placeholder="t('ticket.placeholders.assignee')" style="width: 100%">
            <el-option label="developer1" value="developer1" />
            <el-option label="developer2" value="developer2" />
            <el-option label="developer3" value="developer3" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.dueTime')" prop="dueTime">
          <el-date-picker
            v-model="form.dueTime"
            type="datetime"
            :placeholder="t('ticket.placeholders.dueTime')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="t('ticket.tags')" prop="tags">
          <el-input
            v-model="form.tags"
            :placeholder="t('ticket.placeholders.tags')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { getTicketList, createTicket, updateTicket, getTicketStats } from '@/api/ticket'
import type { Ticket, TicketQuery, TicketForm, TicketStats } from '@/types/ticket'
import { TicketType, TicketPriority, TicketStatus } from '@/types/ticket'

const { t } = useI18n()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const selectedIds = ref<number[]>([])
const ticketList = ref<Ticket[]>([])
const total = ref(0)
const stats = ref<TicketStats>({
  total: 0,
  pending: 0,
  inProgress: 0,
  resolved: 0,
  closed: 0,
  rejected: 0,
  myTickets: 0,
  overdue: 0
})

// 查询表单
const queryForm = reactive<TicketQuery>({
  pageNum: 1,
  pageSize: 20,
  title: '',
  type: undefined,
  priority: undefined,
  status: undefined,
  assignee: '',
  reporter: '',
  startTime: '',
  endTime: '',
  tags: []
})

// 表单数据
const formRef = ref<FormInstance>()
const form = reactive<TicketForm>({
  title: '',
  description: '',
  type: TicketType.BUG,
  priority: TicketPriority.MEDIUM,
  assignee: '',
  dueTime: '',
  tags: [],
  relatedConfigs: [],
  environment: '',
  appName: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  type: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ],
  priority: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ]
}

// 计算属性
const statsData = computed(() => [
  { key: 'total', value: stats.value.total, label: t('ticket.stats.total') },
  { key: 'pending', value: stats.value.pending, label: t('ticket.stats.pending') },
  { key: 'inProgress', value: stats.value.inProgress, label: t('ticket.stats.inProgress') },
  { key: 'resolved', value: stats.value.resolved, label: t('ticket.stats.resolved') },
  { key: 'closed', value: stats.value.closed, label: t('ticket.stats.closed') },
  { key: 'rejected', value: stats.value.rejected, label: t('ticket.stats.rejected') },
  { key: 'myTickets', value: stats.value.myTickets, label: t('ticket.stats.myTickets') },
  { key: 'overdue', value: stats.value.overdue, label: t('ticket.stats.overdue') }
])

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

const loadStats = async () => {
  try {
    stats.value = await getTicketStats()
  } catch (error) {
    console.error('Failed to load stats:', error)
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
    assignee: '',
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

const handleSelectionChange = (selection: Ticket[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    title: '',
    description: '',
    type: TicketType.BUG,
    priority: TicketPriority.MEDIUM,
    assignee: '',
    dueTime: '',
    tags: [],
    relatedConfigs: [],
    environment: '',
    appName: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: Ticket) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title,
    description: row.description,
    type: row.type,
    priority: row.priority,
    assignee: row.assignee,
    dueTime: row.dueTime,
    tags: row.tags,
    relatedConfigs: row.relatedConfigs,
    environment: row.environment,
    appName: row.appName
  })
  dialogVisible.value = true
}

const handleView = (row: Ticket) => {
  router.push(`/ticket/detail/${row.id}`)
}

const handleDelete = async (row: Ticket) => {
  try {
    await ElMessageBox.confirm(
      t('ticket.messages.deleteConfirm', { title: row.title }),
      t('common.confirm'),
      { type: 'warning' }
    )
    // TODO: 调用删除API
    ElMessage.success(t('ticket.messages.deleteSuccess'))
    loadData()
  } catch (error) {
    // 用户取消
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的工单')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 个工单吗？`,
      t('common.confirm'),
      { type: 'warning' }
    )
    // TODO: 调用批量删除API
    ElMessage.success(t('ticket.messages.deleteSuccess'))
    loadData()
  } catch (error) {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateTicket(form.id!, form)
      ElMessage.success(t('ticket.messages.updateSuccess'))
    } else {
      await createTicket(form)
      ElMessage.success(t('ticket.messages.createSuccess'))
    }
    
    dialogVisible.value = false
    loadData()
    loadStats()
  } catch (error) {
    ElMessage.error(isEdit.value ? t('ticket.messages.updateFailed') : t('ticket.messages.createFailed'))
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

// 生命周期
onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.ticket-index {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
}

.stats-content {
  padding: 10px;
}

.stats-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

.search-card {
  margin-bottom: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  gap: 10px;
}

.right {
  display: flex;
  gap: 10px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}
</style> 