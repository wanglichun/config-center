<template>
  <div class="config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="环境">
            <el-select v-model="searchForm.environment" placeholder="请选择环境" clearable style="width: 160px;">
              <el-option v-for="option in environmentOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="配置类别">
            <el-input v-model="searchForm.groupName" placeholder="请输入配置组" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="配置名称">
            <el-input v-model="searchForm.keyword" placeholder="配置键或值关键字" clearable style="width: 200px;" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px;">
              <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="configList" v-loading="loading" style="width: 100%">
        <el-table-column prop="environment" label="环境" width="100">
          <template #default="scope">
            <el-tag :type="getEnvTagType(scope.row.environment)">
              {{ getEnvText(scope.row.environment) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="配置类别" />
        <el-table-column prop="configKey" label="配置名字" />
        <el-table-column prop="configValue" label="配置内容" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="handlePublish(scope.row)">发布</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="isEdit ? '编辑配置' : '新增配置'" width="700px">
      <el-form :model="configForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="configForm.appName" :disabled="isEdit" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="环境" prop="environment">
          <el-select v-model="configForm.environment" placeholder="请选择环境" :disabled="isEdit" style="width: 100%;">
            <el-option v-for="option in environmentOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置组" prop="groupName">
          <el-input v-model="configForm.groupName" :disabled="isEdit" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="configForm.configKey" :disabled="isEdit" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="configForm.dataType" placeholder="请选择数据类型" style="width: 100%;">
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input 
            v-model="configForm.configValue" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入配置值"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="是否加密">
          <el-switch v-model="configForm.encrypted" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="configForm.description" placeholder="请输入配置描述" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getConfigPage, createConfig, updateConfig, deleteConfig, publishConfig } from '@/api/config'
import { getAllEnum, enumToOptions } from '@/api/enum'
import type { ConfigItem, ConfigQuery, ConfigForm } from '@/types/config'
import type { PageResult } from '@/types/common'
import type { AllEnums } from '@/api/enum'

const searchForm = reactive<ConfigQuery>({
  pageNum: 1,
  pageSize: 20,
  appName: '',
  environment: '',
  groupName: '',
  keyword: '',
  status: ''
})

const configForm = reactive<ConfigForm>({
  appName: '',
  environment: '',
  groupName: '',
  configKey: '',
  configValue: '',
  dataType: 'STRING',
  description: '',
  encrypted: false
})

const formRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  environment: [{ required: true, message: '请选择环境', trigger: 'change' }],
  groupName: [{ required: true, message: '请输入配置组', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }]
}

const configList = ref<ConfigItem[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showAddDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()
const loading = ref(false)

// 枚举选项
const environmentOptions = ref<Array<{value: string, label: string}>>([])
const statusOptions = ref<Array<{value: string, label: string}>>([])
const enumsData = ref<AllEnums>()

// 加载枚举数据
const loadEnums = async () => {
  try {
    const response = await getAllEnum()
    if (response.success) {
      enumsData.value = response.data
      environmentOptions.value = enumToOptions(response.data.EnvironmentEnum)
      statusOptions.value = enumToOptions(response.data.ConfigStatusEnum)
      console.log('枚举数据加载成功:', response.data)
    } else {
      console.error('加载枚举失败:', response.message)
      // 使用默认值
      environmentOptions.value = [
        { value: 'dev', label: '开发环境' },
        { value: 'test', label: '测试环境' },
        { value: 'prod', label: '生产环境' }
      ]
      statusOptions.value = [
        { value: 'DRAFT', label: '草稿' },
        { value: 'PUBLISHED', label: '已发布' },
        { value: 'DISABLED', label: '已禁用' }
      ]
    }
  } catch (error) {
    console.error('加载枚举异常:', error)
    // 使用默认值
    environmentOptions.value = [
      { value: 'dev', label: '开发环境' },
      { value: 'test', label: '测试环境' },
      { value: 'prod', label: '生产环境' }
    ]
    statusOptions.value = [
      { value: 'DRAFT', label: '草稿' },
      { value: 'PUBLISHED', label: '已发布' },
      { value: 'DISABLED', label: '已禁用' }
    ]
  }
}

const getEnvTagType = (env: string) => {
  switch (env) {
    case 'dev': return 'primary'
    case 'test': return 'warning'
    case 'prod': return 'danger'
    default: return 'info'
  }
}

const getEnvText = (env: string) => {
  if (enumsData.value?.EnvironmentEnum) {
    return enumsData.value.EnvironmentEnum[env] || env
  }
  // 默认映射
  switch (env) {
    case 'dev': return '开发环境'
    case 'test': return '测试环境'
    case 'prod': return '生产环境'
    default: return env
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'warning'
    case 'DISABLED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  if (enumsData.value?.ConfigStatusEnum) {
    return enumsData.value.ConfigStatusEnum[status] || status
  }
  // 默认映射
  switch (status) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'DISABLED': return '已禁用'
    default: return status
  }
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载配置列表
const loadConfigList = async () => {
  try {
    loading.value = true
    searchForm.pageNum = currentPage.value
    searchForm.pageSize = pageSize.value
    
    const response = await getConfigPage(searchForm)
    if (response.success) {
      const pageResult: PageResult<ConfigItem> = response.data
      configList.value = pageResult.records
      total.value = pageResult.total
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('加载配置列表失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadConfigList()
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.environment = ''
  searchForm.groupName = ''
  searchForm.keyword = ''
  searchForm.status = ''
  currentPage.value = 1
  loadConfigList()
}

const handleEdit = (row: ConfigItem) => {
  isEdit.value = true
  Object.assign(configForm, row)
  showAddDialog.value = true
}

const handlePublish = async (row: ConfigItem) => {
  try {
    await ElMessageBox.confirm(`确定要发布配置 ${row.configKey} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await publishConfig(row.id)
    if (response.success) {
      ElMessage.success('发布成功')
      loadConfigList()
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布配置失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

const handleDelete = async (row: ConfigItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除配置 ${row.configKey} 吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteConfig(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadConfigList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSave = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (valid) {
      let response
      if (isEdit.value && configForm.id) {
        response = await updateConfig(configForm.id, configForm)
      } else {
        response = await createConfig(configForm)
      }

      if (response.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        showAddDialog.value = false
        resetForm()
        loadConfigList()
      } else {
        ElMessage.error(response.message || '保存失败')
      }
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存失败')
  }
}

const resetForm = () => {
  Object.assign(configForm, {
    appName: '',
    environment: '',
    groupName: '',
    configKey: '',
    configValue: '',
    dataType: 'STRING',
    description: '',
    encrypted: false
  })
  isEdit.value = false
  formRef.value?.clearValidate()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadConfigList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadConfigList()
}

onMounted(() => {
  loadEnums()
  loadConfigList()
})
</script>

<style scoped lang="scss">
.config-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
  
  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: flex-end;
    
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
      
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: #606266;
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 响应式设计
@media (max-width: 1400px) {
  .search-bar .el-form {
    .el-form-item {
      flex: 0 0 auto;
      
      // 环境下拉框
      &:nth-child(1) .el-select {
        width: 140px;
      }
      
      // 配置类别输入框
      &:nth-child(2) .el-input {
        width: 160px;
      }
      
      // 配置名称输入框
      &:nth-child(3) .el-input {
        width: 180px;
      }
      
      // 状态下拉框
      &:nth-child(4) .el-select {
        width: 120px;
      }
    }
  }
}

@media (max-width: 768px) {
  .search-bar .el-form {
    flex-direction: column;
    align-items: stretch;
    
    .el-form-item {
      width: 100%;
      
      .el-input,
      .el-select {
        width: 100% !important;
      }
    }
    
    // 按钮组在移动端独占一行
    .el-form-item:last-child {
      display: flex;
      justify-content: center;
      margin-top: 16px;
    }
  }
}
</style> 