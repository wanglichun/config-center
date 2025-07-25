<template>
  <div class="ticket-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            {{ t('common.back') }}
          </el-button>
          <div class="header-actions">
            <el-button type="primary" @click="handleEdit">
              <el-icon><Edit /></el-icon>
              {{ t('ticket.actions.edit') }}
            </el-button>
            <el-button @click="handleAssign">
              <el-icon><User /></el-icon>
              {{ t('ticket.actions.assign') }}
            </el-button>
            <el-button @click="handleUpdateStatus">
              <el-icon><Setting /></el-icon>
              {{ t('ticket.actions.updateStatus') }}
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <div v-if="ticket" class="ticket-content">
          <!-- 工单基本信息 -->
          <el-row :gutter="20">
            <el-col :span="16">
              <div class="ticket-info">
                <h2 class="ticket-title">{{ ticket.title }}</h2>
                <div class="ticket-meta">
                  <el-tag :type="getTypeTagType(ticket.type)" class="meta-tag">
                    {{ t(`ticket.types.${ticket.type}`) }}
                  </el-tag>
                  <el-tag :type="getPriorityTagType(ticket.priority)" class="meta-tag">
                    {{ t(`ticket.priorities.${ticket.priority}`) }}
                  </el-tag>
                  <el-tag :type="getStatusTagType(ticket.status)" class="meta-tag">
                    {{ t(`ticket.statuses.${ticket.status}`) }}
                  </el-tag>
                </div>
                <div class="ticket-description">
                  <h4>{{ t('ticket.description') }}</h4>
                  <p>{{ ticket.description }}</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="ticket-sidebar">
                <el-descriptions :column="1" border>
                  <el-descriptions-item :label="t('ticket.ticketId')">
                    #{{ ticket.id }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.assignee')">
                    {{ ticket.assignee || t('common.none') }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.reporter')">
                    {{ ticket.reporter }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.createTime')">
                    {{ ticket.createTime }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.updateTime')">
                    {{ ticket.updateTime }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.dueTime')">
                    {{ ticket.dueTime || t('common.none') }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.environment')">
                    {{ ticket.environment || t('common.none') }}
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('ticket.appName')">
                    {{ ticket.appName || t('common.none') }}
                  </el-descriptions-item>
                </el-descriptions>

                <div class="tags-section" v-if="ticket.tags && ticket.tags.length > 0">
                  <h4>{{ t('ticket.tags') }}</h4>
                  <div class="tags-list">
                    <el-tag
                      v-for="tag in ticket.tags"
                      :key="tag"
                      size="small"
                      class="tag-item"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 评论和操作记录 -->
          <el-row :gutter="20" class="ticket-bottom">
            <el-col :span="12">
              <div class="comments-section">
                <div class="section-header">
                  <h3>{{ t('ticket.comments') }}</h3>
                  <el-button size="small" @click="showCommentDialog = true">
                    <el-icon><Plus /></el-icon>
                    {{ t('ticket.actions.addComment') }}
                  </el-button>
                </div>
                <div class="comments-list">
                  <div
                    v-for="comment in ticket.comments"
                    :key="comment.id"
                    class="comment-item"
                  >
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.author }}</span>
                      <span class="comment-time">{{ comment.createTime }}</span>
                      <el-tag v-if="comment.isInternal" size="small" type="warning">
                        内部评论
                      </el-tag>
                    </div>
                    <div class="comment-content">
                      {{ comment.content }}
                    </div>
                  </div>
                  <div v-if="ticket.comments.length === 0" class="no-comments">
                    暂无评论
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="operations-section">
                <h3>{{ t('ticket.operations') }}</h3>
                <div class="operations-list">
                  <div class="operation-item">
                    <div class="operation-time">{{ ticket.createTime }}</div>
                    <div class="operation-content">
                      <span class="operation-user">{{ ticket.reporter }}</span>
                      创建了工单
                    </div>
                  </div>
                  <div
                    v-for="operation in operations"
                    :key="operation.id"
                    class="operation-item"
                  >
                    <div class="operation-time">{{ operation.operateTime }}</div>
                    <div class="operation-content">
                      <span class="operation-user">{{ operation.operator }}</span>
                      {{ operation.operation }}
                      <span v-if="operation.details" class="operation-details">
                        ({{ operation.details }})
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 添加评论对话框 -->
    <el-dialog
      v-model="showCommentDialog"
      :title="t('ticket.actions.addComment')"
      width="500px"
    >
      <el-form :model="commentForm" label-width="80px">
        <el-form-item :label="t('ticket.comments')">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            :placeholder="t('ticket.placeholders.comment')"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="commentForm.isInternal">
            内部评论
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCommentDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddComment" :loading="submitting">
          {{ t('common.submit') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配工单对话框 -->
    <el-dialog
      v-model="showAssignDialog"
      :title="t('ticket.actions.assign')"
      width="400px"
    >
      <el-form :model="assignForm" label-width="80px">
        <el-form-item :label="t('ticket.assignee')">
          <el-select v-model="assignForm.assignee" :placeholder="t('ticket.placeholders.assignee')" style="width: 100%">
            <el-option label="developer1" value="developer1" />
            <el-option label="developer2" value="developer2" />
            <el-option label="developer3" value="developer3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAssignSubmit" :loading="submitting">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

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
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, User, Setting, Plus } from '@element-plus/icons-vue'
import { getTicketById } from '@/api/ticket'
import type { Ticket, TicketComment, TicketOperation } from '@/types/ticket'
import { TicketStatus, TicketPriority, TicketType } from '@/types/ticket'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const ticket = ref<Ticket | null>(null)
const operations = ref<TicketOperation[]>([])
const showCommentDialog = ref(false)
const showAssignDialog = ref(false)
const showStatusDialog = ref(false)

// 表单数据
const commentForm = reactive({
  content: '',
  isInternal: false
})

const assignForm = reactive({
  assignee: ''
})

const statusForm = reactive({
  status: TicketStatus.PENDING
})

// 计算属性
const ticketStatuses = computed(() => [
  { value: TicketStatus.PENDING, label: t('ticket.statuses.PENDING') },
  { value: TicketStatus.IN_PROGRESS, label: t('ticket.statuses.IN_PROGRESS') },
  { value: TicketStatus.RESOLVED, label: t('ticket.statuses.RESOLVED') },
  { value: TicketStatus.CLOSED, label: t('ticket.statuses.CLOSED') },
  { value: TicketStatus.REJECTED, label: t('ticket.statuses.REJECTED') }
])

// 方法
const loadData = async () => {
  const ticketId = Number(route.params.id)
  if (!ticketId) {
    ElMessage.error('工单ID无效')
    return
  }

  loading.value = true
  try {
    const response = await getTicketById(ticketId)
    if (response.success) {
      ticket.value = response.data
      // Mock操作记录
      operations.value = [
        {
          id: 1,
          ticketId,
          operation: '更新了状态',
          operator: 'developer1',
          operateTime: '2024-01-16 14:20:00',
          details: '从待处理改为处理中'
        }
      ]
    } else {
      ElMessage.error(response.message || t('ticket.messages.loadFailed'))
    }
  } catch (error) {
    ElMessage.error(t('ticket.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  if (ticket.value) {
    router.push(`/ticket/edit/${ticket.value.id}`)
  }
}

const handleAssign = () => {
  if (ticket.value) {
    assignForm.assignee = ticket.value.assignee || ''
    showAssignDialog.value = true
  }
}

const handleUpdateStatus = () => {
  if (ticket.value) {
    statusForm.status = ticket.value.status
    showStatusDialog.value = true
  }
}

const handleAddComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!ticket.value) return

  submitting.value = true
  try {
    // await addComment(ticket.value.id, commentForm.content, commentForm.isInternal) // Original line commented out
    ElMessage.success(t('ticket.messages.commentSuccess'))
    showCommentDialog.value = false
    commentForm.content = ''
    commentForm.isInternal = false
    loadData() // 重新加载数据以显示新评论
  } catch (error) {
    ElMessage.error('添加评论失败')
  } finally {
    submitting.value = false
  }
}

const handleAssignSubmit = async () => {
  if (!assignForm.assignee) {
    ElMessage.warning('请选择处理人')
    return
  }

  if (!ticket.value) return

  submitting.value = true
  try {
    // await assignTicket(ticket.value.id, assignForm.assignee) // Original line commented out
    ElMessage.success(t('ticket.messages.assignSuccess'))
    showAssignDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    submitting.value = false
  }
}

const handleStatusSubmit = async () => {
  if (!ticket.value) return

  submitting.value = true
  try {
    // await updateTicketStatus(ticket.value.id, statusForm.status) // Original line commented out
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

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ticket-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.ticket-content {
  margin-top: 20px;
}

.ticket-info {
  margin-bottom: 30px;
}

.ticket-title {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #303133;
}

.ticket-meta {
  margin-bottom: 20px;
}

.meta-tag {
  margin-right: 10px;
}

.ticket-description h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.ticket-description p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.ticket-sidebar {
  position: sticky;
  top: 20px;
}

.tags-section {
  margin-top: 20px;
}

.tags-section h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag-item {
  margin-bottom: 5px;
}

.ticket-bottom {
  margin-top: 30px;
}

.comments-section,
.operations-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}

.comments-list {
  max-height: 400px;
  overflow-y: auto;
}

.comment-item {
  background: white;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: bold;
  color: #303133;
  margin-right: 10px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
  margin-right: 10px;
}

.comment-content {
  color: #606266;
  line-height: 1.5;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.operations-list {
  max-height: 400px;
  overflow-y: auto;
}

.operation-item {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.operation-item:last-child {
  border-bottom: none;
}

.operation-time {
  width: 120px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

.operation-content {
  flex: 1;
  color: #606266;
}

.operation-user {
  font-weight: bold;
  color: #303133;
}

.operation-details {
  color: #909399;
  font-style: italic;
}
</style> 