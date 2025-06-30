<template>
  <div class="gray-release-container">
    <div class="header">
      <h2>灰度发布管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建发布计划
      </el-button>
    </div>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="应用名称">
          <el-input v-model="queryForm.appName" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="环境">
          <el-select v-model="queryForm.environment" placeholder="请选择环境" clearable>
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已暂停" value="PAUSED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已失败" value="FAILED" />
            <el-option label="已回滚" value="ROLLBACK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPlans">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发布计划列表 -->
    <el-card class="table-card">
      <el-table :data="plans" v-loading="loading" stripe>
        <el-table-column prop="planName" label="计划名称" />
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="environment" label="环境" />
        <el-table-column prop="grayStrategy" label="灰度策略">
          <template #default="{ row }">
            <el-tag v-if="row.grayStrategy === 'IP_WHITELIST'" type="primary">IP白名单</el-tag>
            <el-tag v-else-if="row.grayStrategy === 'PERCENTAGE'" type="success">按比例</el-tag>
            <el-tag v-else-if="row.grayStrategy === 'CANARY'" type="warning">金丝雀</el-tag>
            <el-tag v-else-if="row.grayStrategy === 'MANUAL_INSTANCES'" type="info">手动选择</el-tag>
            <el-tag v-else>{{ row.grayStrategy }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 'ACTIVE'" type="success">进行中</el-tag>
            <el-tag v-else-if="row.status === 'PAUSED'" type="warning">已暂停</el-tag>
            <el-tag v-else-if="row.status === 'COMPLETED'" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 'FAILED'" type="danger">已失败</el-tag>
            <el-tag v-else-if="row.status === 'ROLLBACK'" type="danger">已回滚</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rolloutPercentage" label="发布进度">
          <template #default="{ row }">
            <el-progress :percentage="row.rolloutPercentage || 0" :status="getProgressStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetails(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="primary" @click="startGrayRelease(row)">
              开始
            </el-button>
            <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" @click="pauseGrayRelease(row)">
              暂停
            </el-button>
            <el-button v-if="row.status === 'PAUSED'" size="small" type="success" @click="resumeGrayRelease(row)">
              恢复
            </el-button>
            <el-button v-if="['ACTIVE', 'PAUSED'].includes(row.status)" size="small" type="success" @click="completeGrayRelease(row)">
              完成
            </el-button>
            <el-button v-if="['ACTIVE', 'PAUSED'].includes(row.status)" size="small" type="danger" @click="rollbackGrayRelease(row)">
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPlans"
        @current-change="loadPlans"
      />
    </el-card>

    <!-- 创建发布计划对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建灰度发布计划" width="800px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="createForm.planName" placeholder="请输入发布计划名称" />
        </el-form-item>
        
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="createForm.appName" placeholder="请输入应用名称" @blur="onFormFieldChange" />
        </el-form-item>
        
        <el-form-item label="环境" prop="environment">
          <el-select v-model="createForm.environment" placeholder="请选择环境" @change="onFormFieldChange">
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="配置组" prop="groupName">
          <el-input v-model="createForm.groupName" placeholder="请输入配置组名称" @blur="onFormFieldChange" />
        </el-form-item>

        <el-form-item label="配置项" prop="configKeys">
          <el-select v-model="createForm.configKeys" multiple placeholder="请选择配置项" @focus="loadConfigKeys">
            <el-option 
              v-for="configKey in availableConfigKeys" 
              :key="configKey" 
              :label="configKey" 
              :value="configKey" />
          </el-select>
        </el-form-item>

        <el-form-item label="灰度策略" prop="grayStrategy">
          <el-select v-model="createForm.grayStrategy" placeholder="请选择灰度策略" @change="onStrategyChange">
            <el-option label="IP白名单" value="IP_WHITELIST" />
            <el-option label="按比例发布" value="PERCENTAGE" />
            <el-option label="金丝雀发布" value="CANARY" />
            <el-option label="手动选择实例" value="MANUAL_INSTANCES" />
          </el-select>
        </el-form-item>

        <!-- IP白名单配置 -->
        <el-form-item v-if="createForm.grayStrategy === 'IP_WHITELIST'" label="IP白名单">
          <el-input
            v-model="ipWhitelistText"
            type="textarea"
            :rows="3"
            placeholder="请输入IP地址，每行一个，例如：&#10;192.168.1.100&#10;192.168.1.101"
          />
        </el-form-item>

        <!-- 按比例发布配置 -->
        <el-form-item v-if="createForm.grayStrategy === 'PERCENTAGE'" label="发布比例">
          <el-slider v-model="createForm.rolloutPercentage" :min="1" :max="100" show-input />
          <span class="form-tip">{{ createForm.rolloutPercentage }}% 的流量将获取灰度配置</span>
        </el-form-item>

        <!-- 手动选择实例 -->
        <el-form-item v-if="createForm.grayStrategy === 'MANUAL_INSTANCES'" label="选择实例">
          <el-button @click="loadInstances" :loading="instancesLoading">刷新实例列表</el-button>
          <el-table 
            :data="availableInstances" 
            @selection-change="handleInstanceSelection"
            style="margin-top: 10px;">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="instanceId" label="实例ID" />
            <el-table-column prop="instanceIp" label="IP地址" />
            <el-table-column prop="instancePort" label="端口" />
            <el-table-column prop="lastHeartbeat" label="最后心跳" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ row.status === 'ACTIVE' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>

        <el-form-item label="发布描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入发布描述" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createPlan" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 计划详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="发布计划详情" width="1000px">
      <div v-if="currentPlan">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="计划名称">{{ currentPlan.planName }}</el-descriptions-item>
          <el-descriptions-item label="应用名称">{{ currentPlan.appName }}</el-descriptions-item>
          <el-descriptions-item label="环境">{{ currentPlan.environment }}</el-descriptions-item>
          <el-descriptions-item label="配置组">{{ currentPlan.groupName }}</el-descriptions-item>
          <el-descriptions-item label="灰度策略">{{ currentPlan.grayStrategy }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentPlan.status }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentPlan.createTime }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ currentPlan.creator }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">配置详情</h3>
        <el-table :data="planDetails" stripe>
          <el-table-column prop="configKey" label="配置键" />
          <el-table-column prop="oldValue" label="原始值" />
          <el-table-column prop="grayValue" label="灰度值" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'PENDING'" type="info">待发布</el-tag>
              <el-tag v-else-if="row.status === 'GRAY'" type="warning">灰度中</el-tag>
              <el-tag v-else-if="row.status === 'PUBLISHED'" type="success">已发布</el-tag>
              <el-tag v-else-if="row.status === 'ROLLBACK'" type="danger">已回滚</el-tag>
              <el-tag v-else>{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { grayReleaseApi, getConfigKeys } from '@/api/config'

const route = useRoute()

// 响应式数据
const loading = ref(false)
const createLoading = ref(false)
const instancesLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const plans = ref([])
const availableInstances = ref([])
const availableConfigKeys = ref([])
const planDetails = ref([])
const currentPlan = ref(null)
const ipWhitelistText = ref('')

// 查询表单
const queryForm = reactive({
  appName: '',
  environment: '',
  status: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 创建表单
const createForm = reactive({
  planName: '',
  appName: '',
  environment: '',
  groupName: '',
  configKeys: [],
  grayStrategy: 'IP_WHITELIST',
  rolloutPercentage: 10,
  description: '',
  selectedInstances: []
})

// 表单验证规则
const createRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  environment: [{ required: true, message: '请选择环境', trigger: 'change' }],
  groupName: [{ required: true, message: '请输入配置组', trigger: 'blur' }],
  configKeys: [{ required: true, message: '请选择配置项', trigger: 'change' }],
  grayStrategy: [{ required: true, message: '请选择灰度策略', trigger: 'change' }]
}

const createFormRef = ref()

// 方法
const loadPlans = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const response = await grayReleaseApi.queryPlans(params)
    plans.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载发布计划失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, {
    appName: '',
    environment: '',
    status: ''
  })
  pagination.pageNum = 1
  loadPlans()
}

const showCreateDialog = () => {
  createDialogVisible.value = true
  resetCreateForm()
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    planName: '',
    appName: '',
    environment: '',
    groupName: '',
    configKeys: [],
    grayStrategy: 'IP_WHITELIST',
    rolloutPercentage: 10,
    description: '',
    selectedInstances: []
  })
  ipWhitelistText.value = ''
  availableInstances.value = []
  availableConfigKeys.value = []
}

const loadConfigKeys = async () => {
  if (!createForm.appName || !createForm.environment) {
    return
  }
  
  try {
    const response = await getConfigKeys({
      appName: createForm.appName,
      environment: createForm.environment,
      groupName: createForm.groupName
    })
    availableConfigKeys.value = response.data
  } catch (error) {
    ElMessage.error('加载配置键失败')
  }
}

const loadInstances = async () => {
  if (!createForm.appName || !createForm.environment) {
    return
  }
  
  instancesLoading.value = true
  try {
    const response = await grayReleaseApi.getInstances({
      appName: createForm.appName,
      environment: createForm.environment,
      groupName: createForm.groupName
    })
    availableInstances.value = response.data
  } catch (error) {
    ElMessage.error('加载实例列表失败')
  } finally {
    instancesLoading.value = false
  }
}

const onStrategyChange = () => {
  if (createForm.grayStrategy === 'MANUAL_INSTANCES') {
    loadInstances()
  }
}

const onFormFieldChange = () => {
  // 当应用名称、环境或配置组发生变化时，重新加载配置键和实例
  loadConfigKeys()
  if (createForm.grayStrategy === 'MANUAL_INSTANCES') {
    loadInstances()
  }
}

const handleInstanceSelection = (selection) => {
  createForm.selectedInstances = selection
}

const createPlan = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    createLoading.value = true
    
    const grayRules = buildGrayRules()
    const planData = {
      ...createForm,
      grayRules: JSON.stringify(grayRules),
      configKeys: JSON.stringify(createForm.configKeys)
    }
    
    let response
    if (createForm.grayStrategy === 'MANUAL_INSTANCES') {
      response = await grayReleaseApi.createPlanWithInstances({
        ...planData,
        selectedInstances: createForm.selectedInstances
      })
    } else {
      response = await grayReleaseApi.createPlan(planData)
    }
    
    if (response.success) {
      ElMessage.success('创建发布计划成功')
      createDialogVisible.value = false
      loadPlans()
    } else {
      ElMessage.error(response.message || '创建发布计划失败')
    }
  } catch (error) {
    console.error('创建发布计划失败:', error)
    ElMessage.error('创建发布计划失败')
  } finally {
    createLoading.value = false
  }
}

const buildGrayRules = () => {
  const rules = {}
  
  switch (createForm.grayStrategy) {
    case 'IP_WHITELIST':
      rules.ipWhitelist = ipWhitelistText.value.split('\n').filter(ip => ip.trim())
      break
    case 'PERCENTAGE':
      rules.percentage = createForm.rolloutPercentage
      break
    case 'CANARY':
      rules.canaryRules = {
        initialPercentage: 5,
        maxPercentage: 50,
        stepPercentage: 10,
        stepInterval: 300 // 5分钟
      }
      break
    case 'MANUAL_INSTANCES':
      rules.selectedInstances = createForm.selectedInstances.map(instance => ({
        instanceId: instance.instanceId,
        instanceIp: instance.instanceIp,
        instancePort: instance.instancePort
      }))
      break
  }
  
  return rules
}

const viewDetails = async (plan) => {
  currentPlan.value = plan
  detailDialogVisible.value = true
  
  try {
    const response = await grayReleaseApi.getPlanDetails(plan.id)
    planDetails.value = response.data
  } catch (error) {
    ElMessage.error('加载计划详情失败')
  }
}

const startGrayRelease = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要开始灰度发布吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await grayReleaseApi.startGrayRelease(plan.id)
    if (response.success) {
      ElMessage.success('灰度发布已开始')
      loadPlans()
    } else {
      ElMessage.error(response.message || '开始灰度发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('开始灰度发布失败')
    }
  }
}

const pauseGrayRelease = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要暂停灰度发布吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await grayReleaseApi.pauseGrayRelease(plan.id)
    if (response.success) {
      ElMessage.success('灰度发布已暂停')
      loadPlans()
    } else {
      ElMessage.error(response.message || '暂停灰度发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('暂停灰度发布失败')
    }
  }
}

const resumeGrayRelease = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要恢复灰度发布吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await grayReleaseApi.resumeGrayRelease(plan.id)
    if (response.success) {
      ElMessage.success('灰度发布已恢复')
      loadPlans()
    } else {
      ElMessage.error(response.message || '恢复灰度发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复灰度发布失败')
    }
  }
}

const completeGrayRelease = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要完成灰度发布吗？这将把灰度配置推送到所有实例。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await grayReleaseApi.completeGrayRelease(plan.id)
    if (response.success) {
      ElMessage.success('灰度发布已完成')
      loadPlans()
    } else {
      ElMessage.error(response.message || '完成灰度发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('完成灰度发布失败')
    }
  }
}

const rollbackGrayRelease = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要回滚灰度发布吗？这将恢复原始配置。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    })
    
    const response = await grayReleaseApi.rollbackGrayRelease(plan.id)
    if (response.success) {
      ElMessage.success('灰度发布已回滚')
      loadPlans()
    } else {
      ElMessage.error(response.message || '回滚灰度发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回滚灰度发布失败')
    }
  }
}

const getProgressStatus = (plan) => {
  if (plan.status === 'COMPLETED') return 'success'
  if (plan.status === 'FAILED' || plan.status === 'ROLLBACK') return 'exception'
  if (plan.status === 'ACTIVE') return 'active'
  return ''
}

// 生命周期
onMounted(() => {
  loadPlans()
  
  // 检查是否从配置页面跳转过来
  if (route.query.action === 'publish') {
    // 使用URL参数预填充表单
    createForm.appName = route.query.appName || ''
    createForm.environment = route.query.environment || ''
    createForm.groupName = route.query.groupName || ''
    
    if (route.query.configKey) {
      createForm.configKeys = [route.query.configKey]
    }
    
    // 自动显示创建对话框
    createDialogVisible.value = true
    
    // 如果有应用名称和环境，自动加载配置键和实例列表
    if (createForm.appName && createForm.environment) {
      loadConfigKeys()
      loadInstances()
    }
  }
})
</script>

<style scoped>
.gray-release-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
</style> 