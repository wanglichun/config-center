<template>
  <div class="config-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('config.detail.title') }}</span>
          <div class="actions">
            <el-button type="primary" @click="handleEdit" icon="Edit">
              {{ $t('common.edit') }}
            </el-button>
            <el-button @click="goBack" icon="ArrowLeft">{{ $t('common.back') }}</el-button>
            <el-button @click="refreshData" icon="Refresh" :loading="loading">{{ $t('common.refresh') }}</el-button>
          </div>
        </div>
      </template>

      <!-- 配置基础信息 -->
      <div class="basic-info">
        <h3>{{ $t('config.detail.basicInfo') }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('config.groupName')">
            {{ configDetail?.groupName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.configKey')">
            {{ configDetail?.configKey }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.dataType')">
            <el-tag>{{ configDetail?.dataType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.status')">
            <el-tag :type="getStatusTagType(configDetail?.status)">
              {{ configDetail?.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('Version')">
            {{ configDetail?.version }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.encrypted')">
            <el-tag :type="configDetail?.encrypted ? 'danger' : 'success'">
              {{ configDetail?.encrypted ? $t('common.yes') : $t('common.no') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('Operator')">
            {{ configDetail?.operator }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('Owner')">
          {{ configDetail?.owner }}
        </el-descriptions-item>
          <el-descriptions-item :label="$t('CreateTime')">
            {{ TimeUtils.formatTime(configDetail?.createTime, 'yyyy-MM-dd HH:mm:ss') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('UpdateTime')">
            {{ TimeUtils.formatTime(configDetail?.updateTime, 'yyyy-MM-dd HH:mm:ss') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.description')" :span="2">
            {{ configDetail?.description || $t('common.none') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.configValue')" :span="2">
            <div class="config-value">
              <el-input
                :model-value="configDetail?.configValue || ''"
                type="textarea"
                :rows="4"
                readonly
                :show-password="configDetail?.encrypted && !showEncrypted"
              />
              <div class="value-actions">
                <el-button 
                  v-if="configDetail?.encrypted" 
                  @click="showEncrypted = !showEncrypted"
                  size="small"
                >
                  {{ showEncrypted ? $t('common.hide') : $t('common.show') }}
                </el-button>
                <el-button @click="copyConfigValue" size="small" icon="CopyDocument">
                  {{ $t('common.copy') }}
                </el-button>
              </div>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 配置变更历史 -->
      <div class="history-section">
        <h3>{{ $t('config.detail.history') }}</h3>
        <div class="history-content">
          <el-table 
            :data="historyList" 
            v-loading="historyLoading"
            style="width: 100%"
            :empty-text="$t('common.noData')"
          >
            <el-table-column prop="title" :label="$t('config.history.ticketTitle')" width="280" show-overflow-tooltip>
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
            <el-table-column prop="applicator" :label="$t('config.history.applicator')" width="280" />
            <el-table-column prop="phase" :label="$t('ticket.phase')" width="280">
              <template #default="{ row }">
                <el-tag :type="getTicketStatusTagType(row.phase)">
                  {{ getTicketStatusText(row.phase) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('config.history.createTime')" width="280">
              <template #default="{ row }">
                {{ TimeUtils.formatTime(row.createTime, 'yyyy-MM-dd HH:mm:ss') }}
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" :label="$t('config.history.updateTime')" width="280">
              <template #default="{ row }">
                {{ TimeUtils.formatTime(row.updateTime, 'yyyy-MM-dd HH:mm:ss') }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('config.operation')" width="150" fixed="right">
              <template #default="scope">
                <div class="operation-buttons">
                  <el-button 
                    size="small" 
                    type="primary" 
                    :disabled="scope.row.phase !== 'Success'"
                    @click="handleRollback(scope.row)"
                  >
                    {{ $t('common.rollback') }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="historyTotal > 0">
            <el-pagination
              v-model:current-page="historyQuery.pageNum"
              v-model:page-size="historyQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="historyTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleHistorySizeChange"
              @current-change="handleHistoryCurrentChange"
            />
          </div>
        </div>
      </div>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { getConfigById, getSubscribedContainers, getConfigHistory } from '@/api/config'
import type { ConfigItem, ConfigHistory } from '@/types/config'
import { useI18n } from 'vue-i18n'
import {TimeUtils} from "@/utils/time.ts";
import type {Ticket} from "@/types/ticket.ts";

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const configDetail = ref<ConfigItem | null>(null)
const loading = ref(false)
const showEncrypted = ref(false)

// 历史相关数据
const historyList = ref<any[]>([])
const historyLoading = ref(false)
const historyTotal = ref(0)
const historyQuery = ref({
  pageNum: 1,
  pageSize: 10,
  configId: 0
})

// 容器详情对话框
const showContainerDetailDialog = ref(false)
const selectedContainerDetail = ref({})

// 计算属性
const configId = computed(() => route.params.id as string)

// 方法
const loadConfigDetail = async () => {
  if (!configId.value) {
    ElMessage.error(t('config.detail.messages.configIdRequired'))
    return
  }

  loading.value = true
  try {
    // 使用ID直接获取配置详情
    const response = await getConfigById(Number(configId.value))
    if (response.success) {
      configDetail.value = response.data
    } else {
      ElMessage.error(response.message || t('config.detail.messages.getConfigDetailFailed'))
    }
  } catch (error) {
    console.error(t('config.detail.messages.getConfigDetailFailed'), error)
    ElMessage.error(t('config.detail.messages.getConfigDetailFailed'))
  } finally {
    loading.value = false
  }
}

const handleTitleClick = (row: Ticket) => {
  router.push(`/ticket/detail/${row.id}`)
}

// 加载配置历史
const loadConfigHistory = async () => {
  if (!configId.value) return
  
  historyLoading.value = true
  try {
    const response = await getConfigHistory(Number(configId.value), {
      pageNum: historyQuery.value.pageNum,
      pageSize: historyQuery.value.pageSize
    })
    if (response.success) {
      historyList.value = response.data.records || []
      historyTotal.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || t('config.detail.messages.getHistoryFailed'))
    }
  } catch (error) {
    console.error(t('config.detail.messages.getHistoryFailed'), error)
    ElMessage.error(t('config.detail.messages.getHistoryFailed'))
  } finally {
    historyLoading.value = false
  }
}

// 历史分页处理
const handleHistorySizeChange = (size: number) => {
  historyQuery.value.pageSize = size
  historyQuery.value.pageNum = 1
  loadConfigHistory()
}

const handleHistoryCurrentChange = (page: number) => {
  historyQuery.value.pageNum = page
  loadConfigHistory()
}

// 操作类型标签颜色
const getOperationTypeTagType = (operationType: string) => {
  switch (operationType) {
    case 'CREATE': return 'success'
    case 'UPDATE': return 'warning'
    case 'DELETE': return 'danger'
    case 'PUBLISH': return 'primary'
    case 'ROLLBACK': return 'info'
    default: return ''
  }
}

// 工单状态标签颜色
const getTicketStatusTagType = (status?: string) => {
  switch (status) {
    case 'Submit': return 'info'
    case 'Reviewing': return 'warning'
    case 'GrayPublish': return 'primary'
    case 'Success': return 'success'
    case 'Rejected': return 'danger'
    case 'Cancelled': return undefined
    default: return undefined
  }
}

// 工单状态文本
const getTicketStatusText = (status?: string) => {
  switch (status) {
    case 'Submit': return t('Submit')
    case 'Reviewing': return t('Reviewing')
    case 'GrayPublish': return t('GrayPublish')
    case 'Success': return t('Success')
    case 'Rejected': return t('Rejected')
    case 'Cancelled': return t('Cancelled')
    default: return status || ''
  }
}

// 格式化JSON数据
const formatJsonData = (jsonString: string) => {
  if (!jsonString) return ''
  try {
    const parsed = JSON.parse(jsonString)
    return JSON.stringify(parsed, null, 2)
  } catch (error) {
    return jsonString
  }
}



const refreshData = () => {
  loadConfigDetail()
  loadConfigHistory()
}

// 复制配置值
const copyConfigValue = async () => {
  try {
    await navigator.clipboard.writeText(configDetail.value?.configValue || '')
    ElMessage.success(t('config.detail.messages.configValueCopied'))
  } catch (error) {
    console.error(t('config.detail.messages.copyFailed'), error)
    ElMessage.error(t('config.detail.messages.copyFailed'))
  }
}

// 复制到剪贴板
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('config.detail.messages.copySuccess'))
  } catch (error) {
    console.error(t('config.detail.messages.copyFailed'), error)
    ElMessage.error(t('config.detail.messages.copyFailed'))
  }
}

// 显示容器详情
const showContainerDetail = (container: any) => {
  selectedContainerDetail.value = container
  showContainerDetailDialog.value = true
}

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  if (configDetail.value) {
    router.push({ name: 'EditConfig', params: { id: configDetail.value.id } })
  }
}

// 处理回滚操作
const handleRollback = (ticket: Ticket) => {
  if (ticket.phase !== 'Success') {
    ElMessage.warning(t('config.detail.messages.onlySuccessTicketCanRollback'))
    return
  }
  
  ElMessageBox.confirm(
    t('config.detail.messages.confirmRollback', { title: ticket.title }),
    t('config.detail.messages.rollbackConfirm'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  ).then(() => {
    // TODO: 调用回滚API
    ElMessage.success(t('config.detail.messages.rollbackSuccess'))
  }).catch(() => {
    // 用户取消
  })
}

const getStatusTagType = (status?: string) => {
  switch (status) {
    case 'Online': return 'success'
    case 'Offline': return 'info'
    case 'Init': return 'danger'
    default: return undefined
  }
}

// 生命周期
onMounted(() => {
  loadConfigDetail()
  loadConfigHistory()
})
</script>

<style scoped lang="scss">
.config-detail {
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

.basic-info {
  margin-bottom: 30px;
  
  h3 {
    margin-bottom: 20px;
    color: #303133;
    font-size: 16px;
    font-weight: 600;
  }
}

.subscribers-info {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

.config-value {
  .value-actions {
    margin-top: 10px;
    display: flex;
    gap: 10px;
  }
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-table) {
  margin-top: 10px;
}

.ip-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.copy-icon {
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
}

.copy-icon:hover {
  color: #337ecc;
}

/* 容器详情对话框 */
.container-detail-content {
  padding: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.detail-item .label {
  font-weight: 500;
  color: #555;
  min-width: 80px;
}

.detail-item .value {
  color: #333;
  flex: 1;
}

.detail-item .config-value {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
  word-break: break-all;
}

/* 历史部分样式 */
.history-section {
  margin-top: 30px;
  
  h3 {
    margin-bottom: 20px;
    color: #303133;
    font-size: 16px;
    font-weight: 600;
  }
}

.history-content {
  .config-value-cell {
    .el-input {
      .el-textarea__inner {
        font-family: monospace;
        font-size: 12px;
        background-color: #f8f9fa;
      }
    }
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// Rollback按钮样式
.operation-buttons {
  .el-button.el-button--primary:disabled {
    background-color: #c0c4cc !important;
    border-color: #c0c4cc !important;
    color: #909399 !important;
    cursor: not-allowed !important;
    
    &:hover {
      background-color: #c0c4cc !important;
      border-color: #c0c4cc !important;
      color: #909399 !important;
    }
  }
}
</style> 