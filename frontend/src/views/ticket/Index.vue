<template>
  <div class="ticket-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('ticket.management') }}</span>
          <div class="actions">
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
            clearable style="width: 200px;"
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
        <el-table-column prop="title" :label="$t('ticket.title')" width="300" show-overflow-tooltip>
          <template #default="scope">
            <el-link
              type="primary"
              :underline="false"
              @click="handleTitleClick(scope.row)"
              class="title-link"
            >
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="phase" :label="$t('ticket.phase')" width="120">
          <template #default="scope">
            <el-tag :type="getPhaseTagType(scope.row.phase)">
              {{ getPhaseText(scope.row.phase) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicator" :label="$t('ticket.applicator')" width="200" />
        <el-table-column prop="createTime" :label="$t('ticket.createTime')" width="200">
          <template #default="scope">
            {{ TimeUtils.formatTime(scope.row.createTime, 'yyyy-MM-dd HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('ticket.updateTime')" width="200">
          <template #default="scope">
            {{ TimeUtils.formatTime(scope.row.updateTime, 'yyyy-MM-dd HH:mm:ss') }}
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
import {TimeUtils} from "@/utils/time.ts";

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
  { value: TicketPhase.Submit, label: t('ticket.phases.submit') },
  { value: TicketPhase.Reviewing, label: t('ticket.phases.reviewing') },
  { value: TicketPhase.Cancelled, label: t('ticket.phases.cancelled') },
  { value: TicketPhase.GrayPublish, label: t('ticket.phases.grayPublish') },
  { value: TicketPhase.Success, label: t('ticket.phases.success') },
  { value: TicketPhase.Rejected, label: t('ticket.phases.rejected') }

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

const handleView = (row: Ticket) => {
  router.push(`/ticket/detail/${row.id}`)
}

const handleTitleClick = (row: Ticket) => {
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
    case TicketPhase.Reviewing:
      return 'info'
    case TicketPhase.GrayPublish:
      return 'warning'
    case TicketPhase.Rejected:
      return 'danger'
    case TicketPhase.Cancelled:
      return 'danger'
    case TicketPhase.Success:
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

.title-link {
  color: #409eff;
  font-weight: 500;
  cursor: pointer;
  
  &:hover {
    color: #66b1ff;
    text-decoration: underline;
  }
}
</style> 