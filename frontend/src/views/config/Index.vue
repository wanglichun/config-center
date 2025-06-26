<template>
  <div class="config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>
      
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
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
          <el-form-item label="配置键">
            <el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="搜索关键词" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="configList" v-loading="loading">
        <el-table-column prop="appName" label="应用名称" width="120" />
        <el-table-column prop="environment" label="环境" width="100" />
        <el-table-column prop="groupName" label="配置组" width="120" />
        <el-table-column prop="configKey" label="配置键" width="150" />
        <el-table-column prop="configValue" label="配置值" show-overflow-tooltip />
        <el-table-column prop="dataType" label="数据类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'warning'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getConfigPage, deleteConfig } from '@/api/config'
import type { ConfigItem, ConfigQuery } from '@/types/config'

const loading = ref(false)
const configList = ref<ConfigItem[]>([])

const searchForm = reactive({
  appName: '',
  environment: '',
  groupName: '',
  configKey: '',
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const showAddDialog = () => {
  ElMessage.info('新增配置功能开发中...')
}

const handleSearch = () => {
  pagination.current = 1
  loadConfigList()
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.environment = ''
  searchForm.groupName = ''
  searchForm.configKey = ''
  searchForm.keyword = ''
  handleSearch()
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = async (row: ConfigItem) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
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

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadConfigList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadConfigList()
}

const loadConfigList = async () => {
  loading.value = true
  try {
    const queryParams: ConfigQuery = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      appName: searchForm.appName || undefined,
      environment: searchForm.environment || undefined,
      groupName: searchForm.groupName || undefined,
      keyword: searchForm.keyword || undefined
    }
    
    const response = await getConfigPage(queryParams)
    if (response.success) {
      configList.value = response.data.data
      pagination.total = response.data.total
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

onMounted(() => {
  loadConfigList()
})
</script>

<style scoped lang="scss">
.config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 