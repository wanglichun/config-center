<template>
  <div class="config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('config.title') }}</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('config.add') }}
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item :label="$t('config.groupName')">
            <el-input v-model="searchForm.groupName" :placeholder="$t('config.placeholders.groupName')" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item :label="$t('config.configKey')">
            <el-input v-model="searchForm.keyword" :placeholder="$t('config.placeholders.keyword')" clearable style="width: 200px;" />
          </el-form-item>
          <el-form-item :label="$t('config.status')">
            <el-select v-model="searchForm.status" :placeholder="$t('common.search')" clearable style="width: 140px;">
              <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
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

      <el-table :data="configList" v-loading="loading" style="width: 100%">
        <el-table-column prop="groupName" :label="$t('config.groupName')" />
        <el-table-column prop="configKey" :label="$t('config.configKey')" />
        <el-table-column prop="configValue" :label="$t('config.configValue')" show-overflow-tooltip />
        <el-table-column prop="description" :label="$t('config.description')" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('config.status')" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('config.createTime')">
          <template #default="scope">
            {{ TimeUtils.formatTime(scope.row.createTime, 'yyyy-MM-dd HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('config.updateTime')">
          <template #default="scope">
            {{ TimeUtils.formatTime(scope.row.updateTime, 'yyyy-MM-dd HH:mm:ss') }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('config.operation')" width="220" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button size="small" type="info" @click="handleViewDetail(scope.row)">
                {{ $t('common.view') }}
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">
                {{ $t('common.edit') }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">
                {{ $t('common.delete') }}
              </el-button>
            </div>
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
    <el-dialog v-model="showAddDialog" :title="isEdit ? $t('config.edit') : $t('config.add')" width="800px" class="config-dialog" @close="handleDialogClose">
      <el-form :model="configForm" label-width="120px" :rules="formRules" ref="formRef" class="dialog-form">
        <div class="form-grid">
          <div class="form-column">
            <el-form-item :label="$t('config.groupName')" prop="groupName">
              <el-input v-model="configForm.groupName" :disabled="isEdit" :placeholder="$t('config.placeholders.groupName')" />
            </el-form-item>
            <el-form-item :label="$t('config.configKey')" prop="configKey">
              <el-input v-model="configForm.configKey" :disabled="isEdit" :placeholder="$t('config.placeholders.configKey')" />
            </el-form-item>
            <el-form-item :label="$t('config.dataType')" prop="dataType">
              <el-select v-model="configForm.dataType" :placeholder="$t('config.dataType')">
                <el-option :label="$t('config.dataTypes.STRING')" value="STRING" />
                <el-option :label="$t('config.dataTypes.JSON')" value="JSON" />
                <el-option :label="$t('config.dataTypes.YAML')" value="YAML" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-column">
            <el-form-item :label="$t('config.encrypted')">
              <el-switch v-model="configForm.encrypted" />
            </el-form-item>
          </div>
        </div>
        <div class="form-full-width">
          <el-form-item :label="$t('config.configValue')" prop="configValue">
            <el-input 
              v-model="configForm.configValue" 
              type="textarea" 
              :rows="4" 
              :placeholder="$t('config.placeholders.configValue')"
              show-word-limit
              maxlength="2000"
            />
          </el-form-item>
          <el-form-item :label="$t('config.description')">
            <el-input 
              v-model="configForm.description" 
              :placeholder="$t('config.placeholders.description')"
              show-word-limit
              maxlength="200"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSave">{{ $t('common.save') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { getConfigPage, createConfig, updateConfig, deleteConfig, publishConfig, getConfigById } from '@/api/config'
import { getAllEnum, enumToOptions } from '@/api/enum'
import type { ConfigItem, ConfigQuery, ConfigForm } from '@/types/config'
import type { PageResult } from '@/types/common'
import type { AllEnums } from '@/api/enum'
import type {Ticket} from "@/types/ticket.ts";
import {TimeUtils} from "@/utils/time.ts";

const { t } = useI18n()
const router = useRouter()

const searchForm = reactive<ConfigQuery>({
  pageNum: 1,
  pageSize: 20,
  groupName: '',
  keyword: '',
  status: ''
})

const configForm = reactive<ConfigForm>({
  groupName: '',
  configKey: '',
  configValue: '',
  dataType: 'STRING',
  description: '',
  encrypted: false
})

const formRules = {
  groupName: [{ required: true, message: t('config.placeholders.groupName'), trigger: 'blur' }],
  configKey: [{ required: true, message: t('config.placeholders.configKey'), trigger: 'blur' }],
  configValue: [{ required: true, message: t('config.placeholders.configValue'), trigger: 'blur' }],
  dataType: [{ required: true, message: t('config.dataType'), trigger: 'change' }]
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
const statusOptions = ref<Array<{value: string, label: string}>>([])
const enumsData = ref<AllEnums>()

// 加载枚举数据
const loadEnums = async () => {
  try {
    const response = await getAllEnum()
    if (response.success) {
      enumsData.value = response.data
      statusOptions.value = enumToOptions(response.data.ConfigStatusEnum)
      console.log(t('config.messages.loadEnumsSuccess'), response.data)
    } else {
      console.error(t('config.messages.loadEnumsFailed'), response.message)
      // 使用默认值
      statusOptions.value = [
        { value: 'DRAFT', label: t('config.statuses.DRAFT') },
        { value: 'PUBLISHED', label: t('config.statuses.PUBLISHED') },
        { value: 'DISABLED', label: t('config.statuses.DISABLED') }
      ]
    }
  } catch (error) {
    console.error(t('config.messages.loadEnumsError'), error)
    // 使用默认值
    statusOptions.value = [
      { value: 'DRAFT', label: t('config.statuses.DRAFT') },
      { value: 'PUBLISHED', label: t('config.statuses.PUBLISHED') },
      { value: 'DISABLED', label: t('config.statuses.DISABLED') }
    ]
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'ONLINE': return 'success'
    case 'OFFLINE': return 'info'
    case 'INIT': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  if (enumsData.value?.ConfigStatusEnum) {
    return enumsData.value.ConfigStatusEnum[status] || status
  }
  // 使用国际化文本
  return t(`config.statuses.${status}`) || status
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
      ElMessage.error(response.message || t('config.messages.loadFailed'))
    }
  } catch (error) {
    console.error(t('config.messages.loadConfigListFailed'), error)
    ElMessage.error(t('config.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadConfigList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    pageNum: 1,
    pageSize: 20,
    groupName: '',
    keyword: '',
    status: ''
  })
  currentPage.value = 1
  loadConfigList()
}

const handleViewDetail = (row: ConfigItem) => {
  router.push({
    name: 'ConfigDetail',
    params: { id: row.id }
  })
}

const handleEdit = (row: ConfigItem) => {
  isEdit.value = true
  // 复制配置数据到表单
  Object.assign(configForm, {
    id: row.id,
    groupName: row.groupName,
    configKey: row.configKey,
    configValue: row.configValue,
    dataType: row.dataType || 'STRING',
    description: row.description || '',
    encrypted: row.encrypted || false,
    tags: row.tags || '',
    remark: row.remark || ''
  })
  // 显示编辑对话框
  showAddDialog.value = true
}

const handlePublish = async (row: ConfigItem) => {
  console.log(t('config.messages.publishConfigInfo'), row)
  
  try {
    // 跳转到配置发布详情页面
    await router.push({
      name: 'ConfigPublishDetail',
      params: { id: row.id }
    })
  } catch (error) {
    console.error('跳转到发布详情页面失败:', error)
    ElMessage.error(t('config.messages.navigateToPublishFailed'))
  }
}

const handleDelete = async (row: ConfigItem) => {
  try {
    await ElMessageBox.confirm(t('config.messages.deleteConfirm', { key: row.configKey }), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    const response = await deleteConfig(row.id)
    if (response.success) {
      ElMessage.success(t('config.messages.deleteSuccess'))
      loadConfigList()
    } else {
      ElMessage.error(response.message || t('config.messages.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(t('config.messages.deleteConfigFailed'), error)
      ElMessage.error(t('config.messages.deleteFailed'))
    }
  }
}

const handleSave = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (valid) {
      let response
      if (isEdit.value && configForm.id) {
        // 获取原始配置信息，确保传递完整的配置数据
        const originalConfigResponse = await getConfigById(configForm.id)
        if (!originalConfigResponse.success) {
          ElMessage.error('获取原始配置信息失败')
          return
        }
        
        const originalConfig = originalConfigResponse.data
        
        // 构建完整的配置信息，包含所有字段
        const completeConfigData = {
          id: originalConfig.id,
          groupName: configForm.groupName,
          configKey: configForm.configKey,
          configValue: configForm.configValue,
          dataType: configForm.dataType,
          description: configForm.description,
          encrypted: configForm.encrypted,
          tags: originalConfig.tags,
          remark: originalConfig.remark,
          version: originalConfig.version,
          status: originalConfig.status,
          zkPath: originalConfig.zkPath,
          lastPublishTime: originalConfig.lastPublishTime,
          publisher: originalConfig.publisher,
          createTime: originalConfig.createTime,
          updateTime: originalConfig.updateTime,
          createBy: originalConfig.createBy,
          updateBy: originalConfig.updateBy,
          delFlag: originalConfig.delFlag
        }
        
        response = await updateConfig(configForm.id, completeConfigData)
      } else {
        response = await createConfig(configForm)
      }

      if (response.success) {
        ElMessage.success(isEdit.value ? t('config.messages.updateSuccess') : t('config.messages.createSuccess'))
        console.log('配置保存响应:', response)
        console.log('isEdit.value:', isEdit.value)
        console.log('configForm.id:', configForm.id)
        
        const isEditMode = isEdit.value && configForm.id
        console.log('条件判断结果:', isEditMode)
        
        showAddDialog.value = false
        resetForm()
        
        if (isEditMode) {
          // 编辑配置成功后，检查是否返回了ticket信息
          console.log('编辑模式，检查返回数据:', response.data)
          console.log('数据类型:', typeof response.data)
          console.log('是否有id属性:', 'id' in response.data)
          
          if (response.data && typeof response.data === 'object' && 'id' in response.data) {
            const ticket = response.data as Ticket
            console.log('跳转到ticket详情页面:', ticket.id)
            router.push(`/ticket/detail/${ticket.id}`)
          } else {
            // 如果没有返回ticket信息，刷新配置列表
            console.log('刷新配置列表')
            loadConfigList()
          }
        } else {
          // 新增配置成功后，刷新配置列表
          console.log('刷新配置列表')
          loadConfigList()
        }
      } else {
        ElMessage.error(response.message || t('config.messages.saveFailed'))
      }
    }
  } catch (error) {
    console.error(t('config.messages.saveConfigFailed'), error)
    ElMessage.error(t('config.messages.saveFailed'))
  }
}

const resetForm = () => {
  Object.assign(configForm, {
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

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showAddDialog.value = true
}

const handleDialogClose = () => {
  showAddDialog.value = false
  resetForm()
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

.operation-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
  
  .el-button {
    margin: 0;
    min-width: 48px;
    padding: 4px 8px;
    font-size: 12px;
    border-radius: 4px;
    
    &.el-button--small {
      height: 26px;
      line-height: 1;
    }
    
    // 编辑按钮样式
    &.el-button--primary {
      background-color: #409eff;
      border-color: #409eff;
      
      &:hover {
        background-color: #66b1ff;
        border-color: #66b1ff;
      }
    }
    
    // 发布按钮样式
    &.el-button--success {
      background-color: #67c23a;
      border-color: #67c23a;
      
      &:hover {
        background-color: #85ce61;
        border-color: #85ce61;
      }
    }
    
    // 删除按钮样式
    &.el-button--danger {
      background-color: #f56c6c;
      border-color: #f56c6c;
      
      &:hover {
        background-color: #f78989;
        border-color: #f78989;
      }
    }
  }
}

// 对话框样式
.config-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-dialog__header) {
    padding: 24px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
  }
  
  .dialog-form {
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
      margin-bottom: 24px;
      
      .form-column {
        .el-form-item {
          margin-bottom: 20px;
          
          :deep(.el-form-item__label) {
            font-weight: 600;
            color: #495057;
            margin-bottom: 8px;
          }
          
          :deep(.el-input),
          :deep(.el-select) {
            width: 100%;
          }
          
          :deep(.el-input__wrapper) {
            border-radius: 6px;
            transition: all 0.3s ease;
            
            &:hover {
              box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            }
            
            &.is-focus {
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }
          
          :deep(.el-switch) {
            --el-switch-on-color: #409eff;
            --el-switch-off-color: #dcdfe6;
          }
        }
      }
    }
    
    .form-full-width {
      .el-form-item {
        margin-bottom: 20px;
        
        :deep(.el-form-item__label) {
          font-weight: 600;
          color: #495057;
          margin-bottom: 8px;
        }
        
        :deep(.el-textarea) {
          .el-textarea__inner {
            border-radius: 6px;
            transition: all 0.3s ease;
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
            
            &:hover {
              box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            }
            
            &:focus {
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }
        }
        
        :deep(.el-input__wrapper) {
          border-radius: 6px;
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
          }
          
          &.is-focus {
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }
        }
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: 12px;
    padding: 20px 0 0;
    border-top: 1px solid #f0f0f0;
    
    .el-button {
      padding: 10px 24px;
      border-radius: 6px;
      font-weight: 500;
      transition: all 0.3s ease;
      
      &.el-button--primary {
        background: linear-gradient(135deg, #409eff, #66b1ff);
        border: none;
        
        &:hover {
          background: linear-gradient(135deg, #66b1ff, #409eff);
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
      }
      
      &:not(.el-button--primary) {
        background: #ffffff;
        border: 1px solid #dcdfe6;
        color: #606266;
        
        &:hover {
          background: #f5f7fa;
          border-color: #c0c4cc;
          transform: translateY(-1px);
        }
      }
    }
  }
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
  
  // 移动端操作按钮优化
  .operation-buttons {
    flex-direction: column;
    gap: 4px;
    
    .el-button {
      width: 100%;
      min-width: auto;
    }
  }
  
  // 移动端对话框优化
  .config-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5vh auto;
    }
    
    .dialog-form {
      .form-grid {
        grid-template-columns: 1fr;
        gap: 16px;
      }
    }
  }
}
</style> 