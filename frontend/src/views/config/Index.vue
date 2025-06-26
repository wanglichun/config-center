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
            <el-select v-model="searchForm.env" placeholder="请选择环境" clearable>
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="生产环境" value="prod" />
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

      <el-table :data="configList" style="width: 100%">
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="env" label="环境" />
        <el-table-column prop="configGroup" label="配置组" />
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="configValue" label="配置值" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showAddDialog" title="配置信息" width="600px">
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="应用名称" required>
          <el-input v-model="configForm.appName" />
        </el-form-item>
        <el-form-item label="环境" required>
          <el-select v-model="configForm.env" placeholder="请选择环境">
            <el-option label="开发环境" value="dev" />
            <el-option label="测试环境" value="test" />
            <el-option label="生产环境" value="prod" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置组" required>
          <el-input v-model="configForm.configGroup" />
        </el-form-item>
        <el-form-item label="配置键" required>
          <el-input v-model="configForm.configKey" />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input v-model="configForm.configValue" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="configForm.description" />
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
  env: ''
})

const configForm = reactive({
  appName: '',
  env: '',
  configGroup: '',
  configKey: '',
  configValue: '',
  description: ''
})

const configList = ref([])
const showAddDialog = ref(false)

const handleSearch = () => {
  // TODO: 实现搜索功能
  ElMessage.info('搜索功能待实现')
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.env = ''
}

const handleEdit = (row: any) => {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能待实现')
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // TODO: 实现删除功能
    ElMessage.success('删除成功')
  })
}

const handleSave = () => {
  // TODO: 实现保存功能
  ElMessage.success('保存成功')
  showAddDialog.value = false
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
</style> 