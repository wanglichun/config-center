<template>
  <div class="history-page">
    <el-card>
      <template #header>
        <span>{{ $t('history.title') }}</span>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item :label="$t('config.appName')">
            <el-input v-model="searchForm.appName" :placeholder="$t('history.enterAppName')" clearable style="width: 200px;" />
          </el-form-item>
          <el-form-item :label="$t('history.operationType')">
            <el-select v-model="searchForm.operation" :placeholder="$t('history.selectOperationType')" clearable style="width: 160px;">
              <el-option :label="$t('operation.create')" value="CREATE" />
              <el-option :label="$t('operation.update')" value="UPDATE" />
              <el-option :label="$t('operation.delete')" value="DELETE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              {{ $t('common.search') }}
            </el-button>
            <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="historyList" style="width: 100%">
        <el-table-column prop="appName" :label="$t('config.appName')" />
        <el-table-column prop="configKey" :label="$t('config.configKey')" />
        <el-table-column prop="operation" :label="$t('history.operationType')">
          <template #default="scope">
            <el-tag :type="getOperationTagType(scope.row.operation)">
              {{ getOperationText(scope.row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" :label="$t('history.operator')" />
        <el-table-column prop="createTime" :label="$t('history.operationTime')" />
        <el-table-column :label="$t('common.actions')" width="120">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">{{ $t('history.viewDetail') }}</el-button>
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
    <el-dialog v-model="showDetailDialog" :title="$t('history.changeDetail')" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('config.appName')">{{ currentRecord.appName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('config.configKey')">{{ currentRecord.configKey }}</el-descriptions-item>
        <el-descriptions-item :label="$t('history.operationType')">{{ getOperationText(currentRecord.operation) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('history.operator')">{{ currentRecord.operator }}</el-descriptions-item>
        <el-descriptions-item :label="$t('history.operationTime')" :span="2">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.oldValue" :label="$t('history.beforeChange')" :span="2">
          <pre>{{ currentRecord.oldValue }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.newValue" :label="$t('history.afterChange')" :span="2">
          <pre>{{ currentRecord.newValue }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
    case 'CREATE': return t('operation.create')
    case 'UPDATE': return t('operation.update')
    case 'DELETE': return t('operation.delete')
    default: return operation
  }
}

const handleSearch = () => {
  // TODO: 实现搜索功能
  console.log(t('history.searchHistory'))
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

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

// 响应式设计
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