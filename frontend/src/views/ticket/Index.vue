<template>
  <div class="ticket-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('ticket.title') }}</span>
          <div class="actions">
            <el-button type="primary" @click="handleAdd" icon="Plus">
              {{ $t('ticket.add') }}
            </el-button>
            <el-button @click="handleRefresh" icon="Refresh" :loading="loading">
              {{ $t('common.refresh') }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item :label="$t('ticket.title')">
          <el-input
            v-model="queryForm.title"
            :placeholder="$t('ticket.placeholders.title')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="$t('ticket.phase')">
          <el-select
            v-model="queryForm.phase"
            :placeholder="$t('ticket.placeholders.phase')"
            clearable
          >
            <el-option
              v-for="phase in phaseOptions"
              :key="phase.value"
              :label="phase.label"
              :value="phase.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ticket.applicator')">
          <el-input
            v-model="queryForm.applicator"
            :placeholder="$t('ticket.placeholders.applicator')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="Search">
            {{ $t('common.search') }}
          </el-button>
          <el-button @click="handleReset" icon="Refresh">
            {{ $t('common.reset') }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" :label="$t('ticket.title')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phase" :label="$t('ticket.phase')" width="120">
          <template #default="scope">
            <el-tag :type="getPhaseTagType(scope.row.phase)">
              {{ getPhaseText(scope.row.phase) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicator" :label="$t('ticket.applicator')" width="120" />
        <el-table-column prop="operator" :label="$t('ticket.operator')" width="120" />
        <el-table-column prop="createTime" :label="$t('ticket.createTime')" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('ticket.updateTime')" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              @click="handleView(scope.row)"
              icon="View"
            >
              {{ $t('common.view') }}
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              icon="Edit"
            >
              {{ $t('common.edit') }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              icon="Delete"
            >
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTicketPage, deleteTicket } from '@/api/ticket'
import type { Ticket, TicketQueryRequest } from '@/types/ticket'
import { TicketPhase } from '@/types/ticket'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

// 响应式数据
const loading = ref(false)
const tableData = ref<Ticket[]>([])
const queryForm = reactive<TicketQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  title: '',
  phase: '',
  applicator: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 工单阶段选项
const phaseOptions = [
  { value: TicketPhase.PENDING, label: t('ticket.phases.pending') },
  { value: TicketPhase.PROCESSING, label: t('ticket.phases.processing') },
  { value: TicketPhase.APPROVED, label: t('ticket.phases.approved') },
  { value: TicketPhase.REJECTED, label: t('ticket.phases.rejected') },
  { value: TicketPhase.COMPLETED, label: t('ticket.phases.completed') }
]

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await getTicketPage(params)
    if (response.success) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.message || t('ticket.messages.loadFailed'))
    }
  } catch (error) {
    console.error('加载工单列表失败', error)
    ElMessage.error(t('ticket.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryForm.title = ''
  queryForm.phase = ''
  queryForm.applicator = ''
  pagination.pageNum = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  loadData()
}

const handleAdd = () => {
  router.push('/ticket/add')
}

const handleView = (row: Ticket) => {
  router.push(`/ticket/detail/${row.id}`)
}

const handleEdit = (row: Ticket) => {
  router.push(`/ticket/edit/${row.id}`)
}

const handleDelete = async (row: Ticket) => {
  try {
    await ElMessageBox.confirm(
      t('ticket.messages.deleteConfirm'),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const response = await deleteTicket(row.id)
    if (response.success) {
      ElMessage.success(t('ticket.messages.deleteSuccess'))
      loadData()
    } else {
      ElMessage.error(response.message || t('ticket.messages.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除工单失败', error)
      ElMessage.error(t('ticket.messages.deleteFailed'))
    }
  }
}

const getPhaseTagType = (phase: string) => {
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

const getPhaseText = (phase: string) => {
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
      return phase
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.ticket-list {
  padding: 20px;
}

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

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 