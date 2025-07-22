<template>
  <div class="publish-detail">
    <!-- 基本信息区域 -->
    <div class="basic-info-section">
      <h2>基本信息</h2>
      <div class="info-grid">
        <div class="info-column">
          <div class="info-item">
            <span class="label">配置组：</span>
            <span class="value">{{ configInfo.groupName }}</span>
          </div>
          <div class="info-item">
            <span class="label">配置键：</span>
            <span class="value">{{ configInfo.configKey }}</span>
          </div>
          <div class="info-item">
            <span class="label">数据类型：</span>
            <span class="value">{{ configInfo.dataType }}</span>
          </div>
        </div>
        <div class="info-column">
          <div class="info-item">
            <span class="label">配置值：</span>
            <span class="value config-value">{{ configInfo.configValue }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="getStatusTagType(configInfo.status)" class="status-tag">
              {{ getStatusText(configInfo.status) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">版本：</span>
            <span class="value">{{ configInfo.version }}</span>
          </div>
        </div>
        <div class="info-column">
          <div class="info-item">
            <span class="label">最后更新：</span>
            <span class="value">{{ formatTime(configInfo.updateTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">发布者：</span>
            <span class="value">{{ configInfo.publisher || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">描述：</span>
            <span class="value">{{ configInfo.description || '-' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布管理区域 -->
    <div class="publish-management">
      <!-- 左侧：可发布容器 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>可发布容器 ({{ availableContainers.length }})</h3>
          <div class="header-actions">
            <el-button type="info" size="small" @click="refreshContainers" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="primary" size="small" @click="publishToSelected" :disabled="selectedContainers.length === 0">
              发布到选中
            </el-button>
            <el-button type="success" size="small" @click="publishToAll" :disabled="availableContainers.length === 0">
              发布到全部
            </el-button>
          </div>
        </div>

        <!-- 筛选器 -->
        <div class="filter-bar">
          <el-form :model="filterForm" inline size="small">
            <el-form-item label="IP">
              <el-input v-model="filterForm.ip" placeholder="搜索IP" clearable style="width: 120px;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="状态" clearable style="width: 100px;">
                <el-option label="Running" value="Running" />
                <el-option label="Success" value="Success" />
                <el-option label="NoGray" value="NoGray" />
                <el-option label="Offline" value="Offline" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFilter">筛选</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 容器列表 -->
        <div class="container-table">
          <el-table 
            :data="filteredAvailableContainers" 
            v-loading="loading"
            @selection-change="handleSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="ip" label="IP" width="140">
              <template #default="scope">
                <div class="ip-cell">
                  <span>{{ scope.row.ip }}</span>
                  <el-icon class="copy-icon" @click="copyToClipboard(scope.row.ip)">
                    <CopyDocument />
                  </el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentVersion" label="当前版本" width="120" />
            <el-table-column prop="configValue" label="当前配置值" width="150" show-overflow-tooltip />
            <el-table-column prop="lastUpdateTime" label="最后更新" width="160">
              <template #default="scope">
                {{ formatTime(scope.row.lastUpdateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="详细信息" width="200">
              <template #default="scope">
                <el-button size="small" type="info" @click="showContainerDetail(scope.row)">
                  查看详情
                </el-button>
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
            :total="filteredAvailableContainers.length"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <!-- 右侧：已发布容器 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>已发布容器 ({{ publishedContainers.length }})</h3>
          <div class="header-actions">
            <el-button type="info" size="small" @click="refreshContainers" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="warning" size="small" @click="rollbackSelected" :disabled="selectedPublishedContainers.length === 0">
              回滚选中
            </el-button>
          </div>
        </div>

        <!-- 筛选器 -->
        <div class="filter-bar">
          <el-form :model="publishedFilterForm" inline size="small">
            <el-form-item label="IP">
              <el-input v-model="publishedFilterForm.ip" placeholder="搜索IP" clearable style="width: 120px;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="publishedFilterForm.status" placeholder="状态" clearable style="width: 100px;">
                <el-option label="Running" value="Running" />
                <el-option label="Success" value="Success" />
                <el-option label="NoGray" value="NoGray" />
                <el-option label="Offline" value="Offline" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePublishedFilter">筛选</el-button>
              <el-button @click="resetPublishedFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 已发布容器列表 -->
        <div class="container-table">
          <el-table 
            :data="filteredPublishedContainers" 
            v-loading="loading"
            @selection-change="handlePublishedSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="ip" label="IP" width="140">
              <template #default="scope">
                <div class="ip-cell">
                  <span>{{ scope.row.ip }}</span>
                  <el-icon class="copy-icon" @click="copyToClipboard(scope.row.ip)">
                    <CopyDocument />
                  </el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentVersion" label="当前版本" width="120" />
            <el-table-column prop="configValue" label="当前配置值" width="150" show-overflow-tooltip />
            <el-table-column prop="publishedVersion" label="发布版本" width="120" />
            <el-table-column prop="publishTime" label="发布时间" width="160">
              <template #default="scope">
                {{ formatTime(scope.row.publishTime) }}
              </template>
            </el-table-column>
            <el-table-column label="详细信息" width="200">
              <template #default="scope">
                <el-button size="small" type="info" @click="showContainerDetail(scope.row)">
                  查看详情
                </el-button>
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
            :total="filteredPublishedContainers.length"
            layout="total, sizes, prev, pager, next"
            @size-change="handlePublishedSizeChange"
            @current-change="handlePublishedCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 发布进度弹窗 -->
    <el-dialog v-model="showPublishProgress" title="发布进度" width="600px" :close-on-click-modal="false">
      <div class="progress-content">
        <div class="progress-item" v-for="item in publishProgress" :key="item.instanceIp">
          <div class="progress-info">
            <span class="instance-name">{{ item.instanceName }}</span>
            <span class="instance-ip">{{ item.instanceIp }}</span>
          </div>
          <div class="progress-status">
            <el-tag :type="getProgressTagType(item.status)" size="small">
              {{ item.statusText }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closePublishProgress">关闭</el-button>
      </template>
    </el-dialog>

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
          <el-tag :type="getStatusTagType(selectedContainerDetail.status)">
            {{ selectedContainerDetail.status }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">当前版本：</span>
          <span class="value">{{ selectedContainerDetail.currentVersion }}</span>
        </div>
        <div class="detail-item">
          <span class="label">当前配置值：</span>
          <span class="value config-value">{{ selectedContainerDetail.configValue }}</span>
        </div>
        <div class="detail-item">
          <span class="label">最后更新：</span>
          <span class="value">{{ formatTime(selectedContainerDetail.lastUpdateTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">发布者：</span>
          <span class="value">{{ selectedContainerDetail.publisher || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">描述：</span>
          <span class="value">{{ selectedContainerDetail.description || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">实例ID：</span>
          <span class="value">{{ selectedContainerDetail.instanceId || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">配置组：</span>
          <span class="value">{{ selectedContainerDetail.groupName || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">配置键：</span>
          <span class="value">{{ selectedContainerDetail.configKey || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">时间戳：</span>
          <span class="value">{{ formatTime(selectedContainerDetail.timestamp) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showContainerDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, Refresh } from '@element-plus/icons-vue'
import { getConfigById, publishConfig, getSubscribedContainers } from '@/api/config'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const configInfo = ref({})
const availableContainers = ref([])
const publishedContainers = ref([])
const loading = ref(false)
const showPublishProgress = ref(false)
const publishProgress = ref([])

// 容器详情对话框
const showContainerDetailDialog = ref(false)
const selectedContainerDetail = ref({})

// 筛选表单
const filterForm = ref({
  ip: '',
  status: ''
})

const publishedFilterForm = ref({
  ip: '',
  status: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const publishedCurrentPage = ref(1)
const publishedPageSize = ref(10)

// 选择状态
const selectedContainers = ref([])
const selectedPublishedContainers = ref([])

// 计算属性
const filteredAvailableContainers = computed(() => {
  let filtered = availableContainers.value
  
  if (filterForm.value.ip) {
    filtered = filtered.filter(item => item.ip.includes(filterForm.value.ip))
  }
  
  if (filterForm.value.status) {
    filtered = filtered.filter(item => item.status === filterForm.value.status)
  }
  
  return filtered
})

const filteredPublishedContainers = computed(() => {
  let filtered = publishedContainers.value
  
  if (publishedFilterForm.value.ip) {
    filtered = filtered.filter(item => item.ip.includes(publishedFilterForm.value.ip))
  }
  
  if (publishedFilterForm.value.status) {
    filtered = filtered.filter(item => item.status === publishedFilterForm.value.status)
  }
  
  return filtered
})

// 获取配置详情
const loadConfigDetail = async () => {
  try {
    const configId = route.params.id
    const response = await getConfigById(Number(configId))
    if (response.success) {
      configInfo.value = response.data
      // 加载容器列表
      await loadContainers()
    } else {
      ElMessage.error(response.message || t('config.publish.messages.getConfigDetailFailed'))
    }
  } catch (error) {
    console.error(t('config.publish.messages.getConfigDetailFailed'), error)
    ElMessage.error(t('config.publish.messages.getConfigDetailFailed'))
  } finally {
    loading.value = false
  }
}

// 获取容器列表
const loadContainers = async () => {
  try {
    loading.value = true
    
    // 调用真实的API获取订阅容器列表
    const configId = route.params.id
    const response = await getSubscribedContainers(Number(configId))
    
    if (response.success) {
      // 将MachineInstance数据映射为容器数据，显示接口返回的完整信息
      const containers = response.data.map((machine) => ({
        ip: machine.ip,
        hostname: machine.name,
        status: machine.status,
        currentVersion: machine.version,
        configValue: machine.configValue || machine.version, // 显示配置值
        lastUpdateTime: machine.lastUpdateTime,
        // 添加更多接口返回的字段
        instanceId: machine.instanceId || machine.name,
        groupName: machine.groupName,
        configKey: machine.configKey,
        timestamp: machine.timestamp || machine.lastUpdateTime,
        published: machine.status === 'Success', // 根据状态判断是否已发布
        publishedVersion: machine.status === 'Success' ? machine.version : '',
        publishTime: machine.status === 'Success' ? machine.lastUpdateTime : null
      }))
      
      // 根据状态分配容器到不同的列表
      availableContainers.value = containers.filter(container => container.status !== 'Success')
      publishedContainers.value = containers.filter(container => container.status === 'Success')
      
      console.log(t('config.publish.messages.loadContainerListSuccess'), containers)
      console.log(t('config.publish.messages.availableContainers'), availableContainers.value)
      console.log(t('config.publish.messages.publishedContainers'), publishedContainers.value)
      console.log(t('config.publish.messages.originalApiData'), response.data)
    } else {
      ElMessage.error(response.message || t('config.publish.messages.getContainerListFailed'))
    }
  } catch (error) {
    console.error(t('config.publish.messages.getContainerListFailed'), error)
    ElMessage.error(t('config.publish.messages.getContainerListFailed'))
  } finally {
    loading.value = false
  }
}

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedContainers.value = selection.map(item => item.ip)
}

const handlePublishedSelectionChange = (selection) => {
  selectedPublishedContainers.value = selection.map(item => item.ip)
}

// 筛选处理
const handleFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  filterForm.value = { ip: '', status: '' }
  currentPage.value = 1
}

const handlePublishedFilter = () => {
  publishedCurrentPage.value = 1
}

const resetPublishedFilter = () => {
  publishedFilterForm.value = { ip: '', status: '' }
  publishedCurrentPage.value = 1
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const handlePublishedSizeChange = (size) => {
  publishedPageSize.value = size
  publishedCurrentPage.value = 1
}

const handlePublishedCurrentChange = (page) => {
  publishedCurrentPage.value = page
}

// 发布操作
const publishToSelected = async () => {
  if (selectedContainers.value.length === 0) {
    ElMessage.warning(t('config.publish.messages.selectContainersToPublish'))
    return
  }
  
  await publishConfigToContainers(selectedContainers.value)
}

const publishToAll = async () => {
  if (availableContainers.value.length === 0) {
    ElMessage.warning(t('config.publish.messages.noContainersToPublish'))
    return
  }
  
  const allContainers = availableContainers.value.map(c => c.ip)
  await publishConfigToContainers(allContainers)
}

const publishConfigToContainers = async (targetContainers) => {
  try {
    showPublishProgress.value = true
    publishProgress.value = targetContainers.map(ip => ({
      instanceIp: ip,
      instanceName: availableContainers.value.find(c => c.ip === ip)?.hostname || ip,
      status: 'pending',
      statusText: '发布中...'
    }))

    // 调用发布接口，传递选中的IP列表
    const configId = route.params.id
    const response = await publishConfig(Number(configId), targetContainers)
    
    if (response.success) {
      // 更新发布进度
      publishProgress.value = publishProgress.value.map(item => ({
        ...item,
        status: 'success',
        statusText: t('config.publish.messages.publishSuccess')
      }))

      ElMessage.success(t('config.publish.messages.publishSuccess'))
      
      // 更新容器状态
      targetContainers.forEach(ip => {
        const container = availableContainers.value.find(c => c.ip === ip)
        if (container) {
          // 更新容器状态为Success
          container.status = 'Success'
          container.published = true
          container.publishedVersion = configInfo.value.version
          container.publishTime = Date.now()
          
          // 移动到已发布列表
          publishedContainers.value.push(container)
          availableContainers.value = availableContainers.value.filter(c => c.ip !== ip)
        }
      })
    } else {
      throw new Error(response.message || t('config.publish.messages.publishFailed'))
    }
    
    setTimeout(() => {
      showPublishProgress.value = false
    }, 2000)

  } catch (error) {
    console.error(t('config.publish.messages.publishFailed'), error)
    publishProgress.value = publishProgress.value.map(item => ({
      ...item,
      status: 'error',
      statusText: t('config.publish.messages.publishFailed')
    }))
    ElMessage.error(t('config.publish.messages.publishFailed') + '：' + error.message)
  }
}

// 回滚操作
const rollbackSelected = async () => {
  if (selectedPublishedContainers.value.length === 0) {
    ElMessage.warning(t('config.publish.messages.selectContainersToRollback'))
    return
  }
  
  try {
    await ElMessageBox.confirm(t('config.publish.messages.confirmRollback'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    // 模拟回滚过程
    ElMessage.success(t('config.publish.messages.rollbackSuccess'))
    
    // 更新容器状态
    selectedPublishedContainers.value.forEach(ip => {
      const container = publishedContainers.value.find(c => c.ip === ip)
      if (container) {
        container.published = false
        delete container.publishedVersion
        delete container.publishTime
        
        // 移动到可用列表
        availableContainers.value.push(container)
        publishedContainers.value = publishedContainers.value.filter(c => c.ip !== ip)
      }
    })
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error(t('config.publish.messages.rollbackFailed'), error)
      ElMessage.error(t('config.publish.messages.rollbackFailed'))
    }
  }
}

// 刷新容器列表
const refreshContainers = async () => {
  loading.value = true
  await loadContainers()
  loading.value = false
  ElMessage.success(t('config.publish.messages.refreshSuccess'))
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('config.publish.messages.copySuccess'))
  } catch (error) {
    console.error(t('config.publish.messages.copyFailed'), error)
    ElMessage.error(t('config.publish.messages.copyFailed'))
  }
}

// 工具函数
const getStatusTagType = (status) => {
  const statusMap = {
    'ACTIVE': 'success',
    'INACTIVE': 'danger',
    'PUBLISHED': 'success',
    'DRAFT': 'warning',
    'Running': 'success',
    'Success': 'success',
    'NoGray': 'warning',
    'Offline': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'ACTIVE': '激活',
    'INACTIVE': '未激活',
    'PUBLISHED': '已发布',
    'DRAFT': '草稿'
  }
  return statusMap[status] || status
}

const getProgressTagType = (status) => {
  const statusMap = {
    'pending': 'warning',
    'success': 'success',
    'error': 'danger'
  }
  return statusMap[status] || 'info'
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

const closePublishProgress = () => {
  showPublishProgress.value = false
}

// 显示容器详情
const showContainerDetail = (container) => {
  selectedContainerDetail.value = container
  showContainerDetailDialog.value = true
}

// 页面加载时获取数据
onMounted(() => {
  loadConfigDetail()
})
</script>

<style scoped>
.publish-detail {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 基本信息区域 */
.basic-info-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.basic-info-section h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.info-item .value {
  color: #333;
  flex: 1;
}

.info-item .config-value {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
  word-break: break-all;
}

.status-tag {
  margin-left: 8px;
}

/* 发布管理区域 */
.publish-management {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.left-panel,
.right-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
}

.panel-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 筛选器 */
.filter-bar {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
}

.filter-bar .el-form {
  margin: 0;
}

.filter-bar .el-form-item {
  margin-bottom: 0;
  margin-right: 16px;
}

/* 容器表格 */
.container-table {
  padding: 0 20px;
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

/* 分页 */
.pagination {
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  background: #fafafa;
  display: flex;
  justify-content: center;
}

/* 发布进度弹窗 */
.progress-content {
  max-height: 400px;
  overflow-y: auto;
}

.progress-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e0e0e0;
}

.progress-item:last-child {
  border-bottom: none;
}

.progress-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instance-name {
  font-weight: 500;
  color: #333;
}

.instance-ip {
  font-size: 12px;
  color: #666;
}

.progress-status {
  flex-shrink: 0;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .publish-management {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .publish-detail {
    padding: 10px;
  }
  
  .panel-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 