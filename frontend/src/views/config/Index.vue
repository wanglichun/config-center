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
          <el-form-item label="应用名称">
            <el-input v-model="searchForm.appName" placeholder="请输入应用名称" clearable />
          </el-form-item>
          <el-form-item label="环境">
            <el-select v-model="searchForm.environment" placeholder="请选择环境" clearable>
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="生产环境" value="prod" />
            </el-select>
          </el-form-item>
          <el-form-item label="配置组">
            <el-input v-model="searchForm.groupName" placeholder="请输入配置组" clearable />
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

      <el-table :data="configList" style="width: 100%">
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="environment" label="环境">
          <template #default="scope">
            <el-tag :type="getEnvTagType(scope.row.environment)">
              {{ getEnvText(scope.row.environment) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="配置组" />
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="configValue" label="配置值" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" />
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
    <el-dialog v-model="showAddDialog" :title="isEdit ? '编辑配置' : '新增配置'" width="600px">
      <el-form :model="configForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="configForm.appName" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="环境" prop="environment">
          <el-select v-model="configForm.environment" placeholder="请选择环境" :disabled="isEdit">
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置组" prop="groupName">
          <el-input v-model="configForm.groupName" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="configForm.configKey" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input 
            v-model="configForm.configValue" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入配置值"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="configForm.description" placeholder="请输入配置描述" />
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

const searchForm = reactive({
  appName: '',
  environment: '',
  groupName: ''
})

const configForm = reactive({
  appName: '',
  environment: '',
  groupName: '',
  configKey: '',
  configValue: '',
  description: ''
})

const formRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  environment: [{ required: true, message: '请选择环境', trigger: 'change' }],
  groupName: [{ required: true, message: '请输入配置组', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

const configList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showAddDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()

const getEnvTagType = (env: string) => {
  switch (env) {
    case 'dev': return 'primary'
    case 'test': return 'warning'
    case 'prod': return 'danger'
    default: return 'info'
  }
}

const getEnvText = (env: string) => {
  switch (env) {
    case 'dev': return '开发环境'
    case 'test': return '测试环境'
    case 'prod': return '生产环境'
    default: return env
  }
}

const handleSearch = () => {
  // TODO: 实现搜索功能
  ElMessage.info('搜索功能待实现')
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.environment = ''
  searchForm.groupName = ''
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(configForm, row)
  showAddDialog.value = true
}

const handlePublish = (row: any) => {
  ElMessageBox.confirm(`确定要发布配置 ${row.configKey} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // TODO: 实现发布功能
    ElMessage.success('发布成功')
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除配置 ${row.configKey} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // TODO: 实现删除功能
    ElMessage.success('删除成功')
  })
}

const handleSave = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      // TODO: 实现保存功能
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showAddDialog.value = false
      resetForm()
    }
  })
}

const resetForm = () => {
  Object.assign(configForm, {
    appName: '',
    environment: '',
    groupName: '',
    configKey: '',
    configValue: '',
    description: ''
  })
  isEdit.value = false
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  // TODO: 重新加载数据
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  // TODO: 重新加载数据
}

onMounted(() => {
  // TODO: 加载配置列表
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
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 