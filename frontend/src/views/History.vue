<template>
  <div class="history-page">
    <el-card>
      <template #header>
        <span>变更历史</span>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="应用名称">
            <el-input v-model="searchForm.appName" placeholder="请输入应用名称" clearable />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-select v-model="searchForm.operation" placeholder="请选择操作类型" clearable>
              <el-option label="新增" value="CREATE" />
              <el-option label="更新" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
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

      <el-table :data="historyList" style="width: 100%">
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="configKey" label="配置键" />
        <el-table-column prop="operation" label="操作类型">
          <template #default="scope">
            <el-tag :type="getOperationTagType(scope.row.operation)">
              {{ getOperationText(scope.row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" />
        <el-table-column prop="createTime" label="操作时间" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="变更详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应用名称">{{ currentRecord.appName }}</el-descriptions-item>
        <el-descriptions-item label="配置键">{{ currentRecord.configKey }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationText(currentRecord.operation) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRecord.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.oldValue" label="变更前" :span="2">
          <pre>{{ currentRecord.oldValue }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.newValue" label="变更后" :span="2">
          <pre>{{ currentRecord.newValue }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'

const searchForm = reactive({
  appName: '',
  operation: ''
})

const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const showDetailDialog = ref(false)
const currentRecord = ref({})

const getOperationTagType = (operation: string) => {
  switch (operation) {
    case 'CREATE': return 'success'
    case 'UPDATE': return 'warning'
    case 'DELETE': return 'danger'
    default: return 'info'
  }
}

const getOperationText = (operation: string) => {
  switch (operation) {
    case 'CREATE': return '新增'
    case 'UPDATE': return '更新'
    case 'DELETE': return '删除'
    default: return operation
  }
}

const handleSearch = () => {
  // TODO: 实现搜索功能
  console.log('搜索历史记录')
}

const handleReset = () => {
  searchForm.appName = ''
  searchForm.operation = ''
}

const handleViewDetail = (row: any) => {
  currentRecord.value = row
  showDetailDialog.value = true
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
  // TODO: 加载历史记录列表
})
</script>

<style scoped lang="scss">
.history-page {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style> 