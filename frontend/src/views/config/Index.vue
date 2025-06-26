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
          <el-form-item label="配置键">
            <el-input v-model="searchForm.key" placeholder="请输入配置键" clearable />
          </el-form-item>
          <el-form-item label="环境">
            <el-select v-model="searchForm.env" placeholder="请选择环境" clearable>
              <el-option label="开发环境" value="dev" />
              <el-option label="测试环境" value="test" />
              <el-option label="生产环境" value="prod" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="configList" v-loading="loading">
        <el-table-column prop="key" label="配置键" />
        <el-table-column prop="value" label="配置值" show-overflow-tooltip />
        <el-table-column prop="env" label="环境" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
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
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const configList = ref([])

const searchForm = reactive({
  key: '',
  env: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
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
  searchForm.key = ''
  searchForm.env = ''
  handleSearch()
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑功能开发中...')
}

const handleDelete = (row: any) => {
  ElMessage.info('删除功能开发中...')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadConfigList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadConfigList()
}

const loadConfigList = () => {
  loading.value = true
  // 模拟数据
  setTimeout(() => {
    configList.value = [
      {
        id: 1,
        key: 'database.url',
        value: 'jdbc:mysql://localhost:3306/config_center',
        env: 'dev',
        description: '数据库连接地址',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 2,
        key: 'redis.host',
        value: 'localhost',
        env: 'dev',
        description: 'Redis主机地址',
        createTime: '2024-01-01 10:05:00'
      }
    ] as any
    pagination.total = 2
    loading.value = false
  }, 1000)
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