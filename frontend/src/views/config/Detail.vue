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
              {{ getStatusText(configDetail?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.version')">
            {{ configDetail?.version }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.encrypted')">
            <el-tag :type="configDetail?.encrypted ? 'danger' : 'success'">
              {{ configDetail?.encrypted ? $t('common.yes') : $t('common.no') }}
            </el-tag>
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
          <el-descriptions-item :label="$t('config.createTime')">
            {{ formatTime(configDetail?.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.updateTime')">
            {{ formatTime(configDetail?.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.createBy')">
            {{ configDetail?.createBy }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('config.updateBy')">
            {{ configDetail?.updateBy }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 订阅容器信息 -->
      <div class="subscribers-info">
        <div class="section-header">
          <h3>{{ $t('config.detail.subscribers') }}</h3>
          <el-button @click="refreshSubscribers" icon="Refresh" :loading="loadingSubscribers" size="small">
            {{ $t('common.refresh') }}
          </el-button>
        </div>
        
        <el-table 
          :data="subscribers" 
          v-loading="loadingSubscribers"
          empty-text="暂无订阅容器"
          stripe
        >
          <el-table-column prop="ip" label="实例IP" min-width="50">
            <template #default="scope">
              <div class="ip-cell">
                <span>{{ scope.row.ip }}</span>
                <el-icon class="copy-icon" @click="copyToClipboard(scope.row.ip)">
                  <CopyDocument />
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="当前版本" width="130" />
          <el-table-column prop="configValue" label="当前配置值" min-width="150" show-overflow-tooltip />
          <el-table-column prop="lastUpdateTime" label="最后更新" width="160">
            <template #default="scope">
              {{ formatTime(scope.row.lastUpdateTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 容器详情对话框 -->
    <el-dialog
      v-model="showContainerDetailDialog"
      title="容器详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="container-detail-content">
        <div class="detail-item">
          <span class="label">IP：</span>
          <span class="value">{{ selectedContainerDetail.ip }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <el-tag :type="selectedContainerDetail.status === 'Running' ? 'success' : 'danger'">
            {{ selectedContainerDetail.status }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">当前版本：</span>
          <span class="value">{{ selectedContainerDetail.version }}</span>
        </div>
        <div class="detail-item">
          <span class="label">当前配置值：</span>
          <span class="value config-value">{{ selectedContainerDetail.configValue }}</span>
        </div>
        <div class="detail-item">
          <span class="label">最后更新：</span>
          <span class="value">{{ formatTime(selectedContainerDetail.lastUpdateTime) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showContainerDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建工单对话框 -->
    <el-dialog
      v-model="showCreateTicketDialog"
      title="创建配置修改工单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ticketFormRef"
        :model="ticketForm"
        :rules="ticketRules"
        label-width="100px"
      >
        <el-form-item label="工单标题" prop="title">
          <el-input
            v-model="ticketForm.title"
            placeholder="请输入工单标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="新配置值" prop="newData">
          <el-input
            v-model="ticketForm.newData"
            type="textarea"
            :rows="6"
            placeholder="请输入新的配置值"
            :show-password="configDetail?.encrypted"
          />
        </el-form-item>
        <el-form-item label="当前配置值">
          <el-input
            :model-value="configDetail?.configValue || ''"
            type="textarea"
            :rows="4"
            readonly
            :show-password="configDetail?.encrypted && !showEncrypted"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTicketDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateTicket" :loading="creatingTicket">
          {{ $t('common.submit') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { getConfigById, getSubscribedContainers } from '@/api/config'
import { createTicket } from '@/api/ticket'
import type { ConfigItem } from '@/types/config'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const configDetail = ref<ConfigItem | null>(null)
const subscribers = ref<any[]>([])
const loading = ref(false)
const loadingSubscribers = ref(false)
const showEncrypted = ref(false)

// 容器详情对话框
const showContainerDetailDialog = ref(false)
const selectedContainerDetail = ref({})

// 创建工单对话框
const showCreateTicketDialog = ref(false)
const ticketFormRef = ref(null)
const ticketForm = ref({
  title: '',
  newData: ''
})
const ticketRules = {
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  newData: [{ required: true, message: '请输入新的配置值', trigger: 'blur' }]
}
const creatingTicket = ref(false)

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
      // 加载订阅者信息
      await loadSubscribers()
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

const loadSubscribers = async () => {
  if (!configDetail.value) return
  
  loadingSubscribers.value = true
  try {
    const configId = Number(route.params.id)
    const response = await getSubscribedContainers(configId)
    if (response.success) {
      // 将MachineInstance数据转换为简单的IP对象数组
      subscribers.value = response.data.map((machine) => ({
        ip: machine.ip,
        status: machine.status,
        version: machine.version,
        configValue: machine.configValue, // 使用API返回的configValue
        lastUpdateTime: machine.lastUpdateTime
      }))
    } else {
      ElMessage.error(response.message || t('config.detail.messages.getSubscribersFailed'))
    }
  } catch (error) {
    console.error(t('config.detail.messages.getSubscribersFailed'), error)
    ElMessage.error(t('config.detail.messages.getSubscribersFailed'))
  } finally {
    loadingSubscribers.value = false
  }
}

const refreshData = () => {
  loadConfigDetail()
}

const refreshSubscribers = () => {
  loadSubscribers()
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
const showContainerDetail = (container) => {
  selectedContainerDetail.value = container
  showContainerDetailDialog.value = true
}

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  showCreateTicketDialog.value = true
  // 预填充工单标题
  ticketForm.value.title = `修改配置项: ${configDetail.value?.configKey}`
  ticketForm.value.newData = configDetail.value?.configValue || ''
}

const handleCreateTicket = async () => {
  if (!ticketFormRef.value) return

  try {
    await ticketFormRef.value.validate()
    creatingTicket.value = true
    
    const configId = Number(route.params.id)
    const response = await createTicket({
      configId,
      title: ticketForm.value.title,
      newData: ticketForm.value.newData
    })
    
    if (response.success && response.data) {
      ElMessage.success('工单创建成功')
      showCreateTicketDialog.value = false
      // 跳转到工单详情页面
      router.push(`/ticket/detail/${response.data}`)
    } else {
      ElMessage.error(response.message || '工单创建失败')
    }
  } catch (error) {
    console.error('创建工单失败', error)
    ElMessage.error('工单创建失败')
  } finally {
    creatingTicket.value = false
  }
}

const getStatusTagType = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'DRAFT': return 'info'
    case 'DELETED': return 'danger'
    default: return undefined
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return '已发布'
    case 'DRAFT': return '草稿'
    case 'DELETED': return '已删除'
    default: return status || ''
  }
}

const formatTime = (time?: string | number) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadConfigDetail()
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
</style> 