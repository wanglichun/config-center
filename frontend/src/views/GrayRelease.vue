<template>
  <div class="gray-release">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('grayRelease.title') }}</span>
        </div>
      </template>

      <!-- 发布计划列表 -->
      <div class="plan-list">
        <div class="list-header">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            {{ t('grayRelease.createPlan') }}
          </el-button>
          <el-button @click="loadPlans">
            <el-icon><Refresh /></el-icon>
            {{ t('common.refresh') }}
          </el-button>
        </div>

        <el-table :data="planList" v-loading="loading">
          <el-table-column prop="planName" :label="t('grayRelease.planName')" />
          <el-table-column prop="appName" :label="t('config.appName')" />
          <el-table-column prop="environment" :label="t('config.environment')" />
          <el-table-column prop="configKey" :label="t('config.configKey')" />
          <el-table-column prop="strategy" :label="t('grayRelease.strategy')">
            <template #default="scope">
              <el-tag>{{ getStrategyLabel(scope.row.strategy) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('grayRelease.status')">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="t('common.createTime')" />
          <el-table-column :label="t('common.actions')" width="200">
            <template #default="scope">
              <el-button size="small" @click="viewPlan(scope.row)">
                {{ t('common.view') }}
              </el-button>
              <el-button 
                v-if="scope.row.status === 'DRAFT'" 
                size="small" 
                type="primary" 
                @click="executePlan(scope.row)"
              >
                {{ t('grayRelease.execute') }}
              </el-button>
              <el-button 
                v-if="scope.row.status === 'EXECUTING'" 
                size="small" 
                type="success" 
                @click="completePlan(scope.row)"
              >
                {{ t('grayRelease.complete') }}
              </el-button>
              <el-button 
                v-if="['EXECUTING', 'COMPLETED'].includes(scope.row.status)" 
                size="small" 
                type="warning" 
                @click="rollbackPlan(scope.row)"
              >
                {{ t('grayRelease.rollback') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 创建发布计划对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="t('grayRelease.createPlan')" 
      width="800px"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <el-form-item :label="t('grayRelease.planName')" prop="planName">
          <el-input v-model="createForm.planName" />
        </el-form-item>
        
        <el-form-item :label="t('config.appName')" prop="appName">
          <el-input v-model="createForm.appName" />
        </el-form-item>
        
        <el-form-item :label="t('config.environment')" prop="environment">
          <el-select v-model="createForm.environment" style="width: 100%">
            <el-option 
              v-for="env in environments" 
              :key="env.value" 
              :label="env.label" 
              :value="env.value" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="t('config.groupName')" prop="groupName">
          <el-input v-model="createForm.groupName" />
        </el-form-item>
        
        <el-form-item :label="t('config.configKey')" prop="configKey">
          <el-input v-model="createForm.configKey" />
        </el-form-item>
        
        <el-form-item :label="t('grayRelease.strategy')" prop="strategy">
          <el-select v-model="createForm.strategy" @change="onStrategyChange" style="width: 100%">
            <el-option 
              v-for="strategy in strategies" 
              :key="strategy.value" 
              :label="strategy.label" 
              :value="strategy.value" 
            />
          </el-select>
        </el-form-item>

        <!-- 实例选择 -->
        <el-form-item :label="t('grayRelease.instances')" prop="instances">
          <div class="instance-selection">
            <div class="instance-header">
              <el-button size="small" @click="loadInstances">
                <el-icon><Refresh /></el-icon>
                {{ t('grayRelease.loadInstances') }}
              </el-button>
              <span class="instance-count">
                {{ t('grayRelease.selectedCount', { count: selectedInstances.length, total: instanceList.length }) }}
              </span>
            </div>
            
            <div class="instance-list" v-loading="instanceLoading">
              <el-checkbox-group v-model="selectedInstances">
                <div class="instance-grid">
                  <el-checkbox 
                    v-for="instance in instanceList" 
                    :key="instance.id" 
                    :label="instance.id"
                    class="instance-item"
                  >
                    <div class="instance-info">
                      <div class="instance-ip">{{ instance.ip }}:{{ instance.port }}</div>
                      <div class="instance-status">
                        <el-tag size="small" :type="instance.status === 'ONLINE' ? 'success' : 'danger'">
                          {{ instance.status }}
                        </el-tag>
                      </div>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>

        <!-- 策略配置 -->
        <el-form-item v-if="createForm.strategy === 'PERCENTAGE'" :label="t('grayRelease.percentage')">
          <el-slider v-model="createForm.percentage" :max="100" show-input />
        </el-form-item>

        <el-form-item v-if="createForm.strategy === 'IP_WHITELIST'" :label="t('grayRelease.ipWhitelist')">
          <el-input 
            v-model="createForm.ipWhitelist" 
            type="textarea" 
            :placeholder="t('grayRelease.ipWhitelistPlaceholder')" 
          />
        </el-form-item>

        <el-form-item :label="t('grayRelease.description')">
          <el-input v-model="createForm.description" type="textarea" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="createPlan">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 计划详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      :title="t('grayRelease.planDetail')" 
      width="1000px"
    >
      <div v-if="currentPlan">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="t('grayRelease.planName')">
            {{ currentPlan.planName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('grayRelease.status')">
            <el-tag :type="getStatusType(currentPlan.status)">
              {{ getStatusLabel(currentPlan.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('config.appName')">
            {{ currentPlan.appName }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('config.environment')">
            {{ currentPlan.environment }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('config.configKey')">
            {{ currentPlan.configKey }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('grayRelease.strategy')">
            {{ getStrategyLabel(currentPlan.strategy) }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createTime')">
            {{ currentPlan.createTime }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.updateTime')">
            {{ currentPlan.updateTime }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="plan-instances" style="margin-top: 20px;">
          <h4>{{ t('grayRelease.planInstances') }}</h4>
          <el-table :data="planDetails" size="small">
            <el-table-column prop="instanceId" :label="t('grayRelease.instanceId')" />
            <el-table-column prop="instanceIp" :label="t('grayRelease.instanceIp')" />
            <el-table-column prop="status" :label="t('grayRelease.status')">
              <template #default="scope">
                <el-tag size="small" :type="getDetailStatusType(scope.row.status)">
                  {{ getDetailStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executeTime" :label="t('grayRelease.executeTime')" />
            <el-table-column prop="errorMessage" :label="t('grayRelease.errorMessage')" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { grayReleaseApi } from '@/api/config'

const { t } = useI18n()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const instanceLoading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const createFormRef = ref()

const planList = ref([])
const instanceList = ref([])
const selectedInstances = ref([])
const planDetails = ref([])
const currentPlan = ref(null)

// 表单数据
const createForm = reactive({
  planName: '',
  appName: '',
  environment: '',
  groupName: '',
  configKey: '',
  strategy: 'MANUAL',
  percentage: 10,
  ipWhitelist: '',
  description: '',
  instances: []
})

// 表单验证规则
const createRules = {
  planName: [{ required: true, message: t('grayRelease.planNameRequired'), trigger: 'blur' }],
  appName: [{ required: true, message: t('config.appNameRequired'), trigger: 'blur' }],
  environment: [{ required: true, message: t('config.environmentRequired'), trigger: 'change' }],
  configKey: [{ required: true, message: t('config.configKeyRequired'), trigger: 'blur' }],
  strategy: [{ required: true, message: t('grayRelease.strategyRequired'), trigger: 'change' }]
}

// 选项数据
const environments = [
  { label: 'DEV', value: 'DEV' },
  { label: 'TEST', value: 'TEST' },
  { label: 'STAGING', value: 'STAGING' },
  { label: 'PROD', value: 'PROD' }
]

const strategies = [
  { label: t('grayRelease.strategies.manual'), value: 'MANUAL' },
  { label: t('grayRelease.strategies.percentage'), value: 'PERCENTAGE' },
  { label: t('grayRelease.strategies.ipWhitelist'), value: 'IP_WHITELIST' },
  { label: t('grayRelease.strategies.canary'), value: 'CANARY' }
]

// 方法
const loadPlans = async () => {
  loading.value = true
  try {
    const response = await grayReleaseApi.getPlans({})
    if (response.success) {
      planList.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载发布计划失败:', error)
    ElMessage.error(t('grayRelease.loadPlansError'))
  } finally {
    loading.value = false
  }
}

const loadInstances = async () => {
  if (!createForm.appName || !createForm.environment) {
    ElMessage.warning(t('grayRelease.selectAppAndEnv'))
    return
  }

  instanceLoading.value = true
  try {
    const response = await grayReleaseApi.getInstances({
      appName: createForm.appName,
      environment: createForm.environment
    })
    if (response.success) {
      instanceList.value = response.data || []
    }
  } catch (error) {
    console.error('加载实例失败:', error)
    ElMessage.error(t('grayRelease.loadInstancesError'))
  } finally {
    instanceLoading.value = false
  }
}

const createPlan = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    if (selectedInstances.value.length === 0) {
      ElMessage.warning(t('grayRelease.selectInstances'))
      return
    }

    const planData = {
      ...createForm,
      instances: selectedInstances.value
    }

    const response = await grayReleaseApi.createPlan(planData)
    if (response.success) {
      ElMessage.success(t('grayRelease.createSuccess'))
      showCreateDialog.value = false
      resetCreateForm()
      loadPlans()
    } else {
      ElMessage.error(response.message || t('grayRelease.createError'))
    }
  } catch (error) {
    console.error('创建发布计划失败:', error)
    ElMessage.error(t('grayRelease.createError'))
  }
}

const viewPlan = async (plan) => {
  currentPlan.value = plan
  showDetailDialog.value = true
  
  try {
    const response = await grayReleaseApi.getPlanDetails(plan.id)
    if (response.success) {
      planDetails.value = response.data || []
    }
  } catch (error) {
    console.error('加载计划详情失败:', error)
  }
}

const executePlan = async (plan) => {
  try {
    await ElMessageBox.confirm(
      t('grayRelease.executeConfirm', { name: plan.planName }),
      t('common.confirm'),
      { type: 'warning' }
    )

    const response = await grayReleaseApi.executePlan(plan.id)
    if (response.success) {
      ElMessage.success(t('grayRelease.executeSuccess'))
      loadPlans()
    } else {
      ElMessage.error(response.message || t('grayRelease.executeError'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行发布计划失败:', error)
      ElMessage.error(t('grayRelease.executeError'))
    }
  }
}

const completePlan = async (plan) => {
  try {
    await ElMessageBox.confirm(
      t('grayRelease.completeConfirm', { name: plan.planName }),
      t('common.confirm'),
      { type: 'success' }
    )

    const response = await grayReleaseApi.completePlan(plan.id)
    if (response.success) {
      ElMessage.success(t('grayRelease.completeSuccess'))
      loadPlans()
    } else {
      ElMessage.error(response.message || t('grayRelease.completeError'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成发布计划失败:', error)
      ElMessage.error(t('grayRelease.completeError'))
    }
  }
}

const rollbackPlan = async (plan) => {
  try {
    await ElMessageBox.confirm(
      t('grayRelease.rollbackConfirm', { name: plan.planName }),
      t('common.confirm'),
      { type: 'warning' }
    )

    const response = await grayReleaseApi.rollbackPlan(plan.id)
    if (response.success) {
      ElMessage.success(t('grayRelease.rollbackSuccess'))
      loadPlans()
    } else {
      ElMessage.error(response.message || t('grayRelease.rollbackError'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回滚发布计划失败:', error)
      ElMessage.error(t('grayRelease.rollbackError'))
    }
  }
}

const onStrategyChange = () => {
  selectedInstances.value = []
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    planName: '',
    appName: '',
    environment: '',
    groupName: '',
    configKey: '',
    strategy: 'MANUAL',
    percentage: 10,
    ipWhitelist: '',
    description: '',
    instances: []
  })
  selectedInstances.value = []
  instanceList.value = []
}

// 状态和策略标签
const getStatusLabel = (status) => {
  const labels = {
    DRAFT: t('grayRelease.statuses.draft'),
    EXECUTING: t('grayRelease.statuses.executing'),
    COMPLETED: t('grayRelease.statuses.completed'),
    FAILED: t('grayRelease.statuses.failed'),
    CANCELLED: t('grayRelease.statuses.cancelled')
  }
  return labels[status] || status
}

const getStatusType = (status) => {
  const types = {
    DRAFT: 'info',
    EXECUTING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return types[status] || 'info'
}

const getStrategyLabel = (strategy) => {
  const labels = {
    MANUAL: t('grayRelease.strategies.manual'),
    PERCENTAGE: t('grayRelease.strategies.percentage'),
    IP_WHITELIST: t('grayRelease.strategies.ipWhitelist'),
    CANARY: t('grayRelease.strategies.canary')
  }
  return labels[strategy] || strategy
}

const getDetailStatusLabel = (status) => {
  const labels = {
    PENDING: t('grayRelease.detailStatuses.pending'),
    SUCCESS: t('grayRelease.detailStatuses.success'),
    FAILED: t('grayRelease.detailStatuses.failed')
  }
  return labels[status] || status
}

const getDetailStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return types[status] || 'info'
}

// 初始化
onMounted(() => {
  // 从路由参数中获取配置信息
  const { action, appName, environment, groupName, configKey } = route.query
  
  if (action === 'publish' && appName && environment && configKey) {
    // 自动填充表单并显示创建对话框
    createForm.appName = appName as string
    createForm.environment = environment as string
    createForm.groupName = (groupName as string) || ''
    createForm.configKey = configKey as string
    createForm.planName = `${appName}-${configKey}-${Date.now()}`
    
    showCreateDialog.value = true
    
    // 自动加载实例
    setTimeout(() => {
      loadInstances()
    }, 500)
  }
  
  loadPlans()
})
</script>

<style scoped>
.gray-release {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.instance-selection {
  width: 100%;
}

.instance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.instance-count {
  font-size: 14px;
  color: #666;
}

.instance-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.instance-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.instance-item {
  margin: 0;
}

.instance-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instance-ip {
  font-weight: 500;
}

.instance-status {
  display: flex;
  align-items: center;
}

.plan-instances {
  margin-top: 20px;
}
</style> 