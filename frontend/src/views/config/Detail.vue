<template>
  <div class="config-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('config.detail.title') }}</span>
          <div class="actions">
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
          <el-table-column prop="instanceIp" label="实例IP" min-width="150" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfig } from '@/api/config'
import { machineConfigApi } from '@/api/config'
import type { ConfigItem } from '@/types/config'

const route = useRoute()
const router = useRouter()

// 响应式数据
const configDetail = ref<ConfigItem | null>(null)
const subscribers = ref<any[]>([])
const loading = ref(false)
const loadingSubscribers = ref(false)
const showEncrypted = ref(false)

// 计算属性
const configId = computed(() => route.params.id as string)

// 方法
const loadConfigDetail = async () => {
  if (!configId.value) {
    ElMessage.error('配置ID不能为空')
    return
  }

  loading.value = true
  try {
    // 这里需要根据实际的路由参数来获取配置详情
    // 假设路由参数包含必要的信息
    const params = {
      appName: route.query.appName as string,
      environment: route.query.environment as string,
      groupName: route.query.groupName as string,
      configKey: route.query.configKey as string
    }
    
    const response = await getConfig(params)
    if (response.success) {
      configDetail.value = response.data
      // 加载订阅者信息
      await loadSubscribers()
    } else {
      ElMessage.error(response.message || '获取配置详情失败')
    }
  } catch (error) {
    console.error('获取配置详情失败:', error)
    ElMessage.error('获取配置详情失败')
  } finally {
    loading.value = false
  }
}

const loadSubscribers = async () => {
  if (!configDetail.value) return
  
  loadingSubscribers.value = true
  try {
    const params = {
      appName: route.query.appName as string || 'default',
      environment: route.query.environment as string || 'dev',
      groupName: configDetail.value.groupName,
      configKey: configDetail.value.configKey
    }
    
    const response = await machineConfigApi.getSubscribers(params)
    if (response.success) {
      // 将订阅者ID转换为简单的IP对象数组
      subscribers.value = response.data.map((instanceId: string) => ({
        instanceIp: instanceId.split(':')[0] || instanceId // 提取IP部分
      }))
    } else {
      ElMessage.error(response.message || '获取订阅者信息失败')
    }
  } catch (error) {
    console.error('获取订阅者信息失败:', error)
    ElMessage.error('获取订阅者信息失败')
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

const copyConfigValue = async () => {
  if (!configDetail.value?.configValue) return
  
  try {
    await navigator.clipboard.writeText(configDetail.value.configValue)
    ElMessage.success('配置值已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const goBack = () => {
  router.back()
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
</style> 