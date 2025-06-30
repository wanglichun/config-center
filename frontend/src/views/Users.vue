<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('users.title') }}</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            {{ $t('users.addUser') }}
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item :label="$t('users.username')">
            <el-input v-model="searchForm.username" :placeholder="$t('users.enterUsername')" clearable style="width: 200px;" />
          </el-form-item>
          <el-form-item :label="$t('users.role')">
            <el-select v-model="searchForm.role" :placeholder="$t('users.selectRole')" clearable style="width: 160px;">
              <el-option :label="$t('roles.admin')" value="ADMIN" />
              <el-option :label="$t('roles.developer')" value="DEVELOPER" />
              <el-option :label="$t('roles.viewer')" value="VIEWER" />
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

      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="username" :label="$t('users.username')" />
        <el-table-column prop="email" :label="$t('users.email')" />
        <el-table-column prop="role" :label="$t('users.role')">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('users.status')">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? $t('users.active') : $t('users.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('users.createTime')" />
        <el-table-column :label="$t('common.actions')" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">{{ $t('common.edit') }}</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? $t('users.disable') : $t('users.enable') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="isEdit ? $t('users.editUser') : $t('users.addUser')" width="550px">
      <el-form :model="userForm" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item :label="$t('users.username')" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="$t('users.email')" prop="email">
          <el-input v-model="userForm.email" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="!isEdit" :label="$t('users.password')" prop="password">
          <el-input v-model="userForm.password" type="password" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="$t('users.role')" prop="role">
          <el-select v-model="userForm.role" :placeholder="$t('users.selectRole')" style="width: 100%;">
            <el-option :label="$t('roles.admin')" value="ADMIN" />
            <el-option :label="$t('roles.developer')" value="DEVELOPER" />
            <el-option :label="$t('roles.viewer')" value="VIEWER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const searchForm = reactive({
  username: '',
  role: ''
})

const userForm = reactive({
  username: '',
  email: '',
  password: '',
  role: ''
})

const formRules = {
  username: [{ required: true, message: t('users.usernameRequired'), trigger: 'blur' }],
  email: [
    { required: true, message: t('users.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('users.emailInvalid'), trigger: 'blur' }
  ],
  password: [{ required: true, message: t('users.passwordRequired'), trigger: 'blur' }],
  role: [{ required: true, message: t('users.roleRequired'), trigger: 'change' }]
}

const userList = ref([])
const showAddDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()

const getRoleTagType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'DEVELOPER': return 'warning'
    case 'VIEWER': return 'info'
    default: return 'info'
  }
}

const getRoleText = (role: string) => {
  switch (role) {
    case 'ADMIN': return t('roles.admin')
    case 'DEVELOPER': return t('roles.developer')
    case 'VIEWER': return t('roles.viewer')
    default: return role
  }
}

const handleSearch = () => {
  // TODO: 实现搜索功能
  ElMessage.info(t('users.searchTodoMessage'))
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.role = ''
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(userForm, row)
  showAddDialog.value = true
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 'ACTIVE' ? t('users.disable') : t('users.enable')
  ElMessageBox.confirm(t('users.confirmToggleStatus', { action, username: row.username }), t('common.confirm'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(() => {
    // TODO: 实现状态切换功能
    ElMessage.success(t('users.toggleStatusSuccess', { action }))
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(t('users.confirmDelete', { username: row.username }), t('common.confirm'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(() => {
    // TODO: 实现删除功能
    ElMessage.success(t('users.deleteSuccess'))
  })
}

const handleSave = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      // TODO: 实现保存功能
      ElMessage.success(isEdit.value ? t('users.updateSuccess') : t('users.createSuccess'))
      showAddDialog.value = false
      resetForm()
    }
  })
}

const resetForm = () => {
  Object.assign(userForm, {
    username: '',
    email: '',
    password: '',
    role: ''
  })
  isEdit.value = false
}

onMounted(() => {
  // TODO: 加载用户列表
})
</script>

<style scoped lang="scss">
.users-page {
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