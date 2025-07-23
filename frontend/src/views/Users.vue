<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('users.title') }}</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('users.addUser') }}
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item :label="$t('users.username')">
            <el-input v-model="searchForm.keyword" :placeholder="$t('users.enterUsername')" clearable style="width: 200px;" />
          </el-form-item>
          <el-form-item :label="$t('users.role')">
            <el-select v-model="searchForm.role" :placeholder="$t('users.selectRole')" clearable style="width: 160px;">
              <el-option :label="$t('roles.admin')" value="ADMIN" />
              <el-option :label="$t('roles.developer')" value="DEVELOPER" />
              <el-option :label="$t('roles.viewer')" value="VIEWER" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('users.status')">
            <el-select v-model="searchForm.status" :placeholder="$t('users.selectStatus')" clearable style="width: 120px;">
              <el-option :label="$t('users.active')" value="ACTIVE" />
              <el-option :label="$t('users.inactive')" value="INACTIVE" />
              <el-option :label="$t('users.locked')" value="LOCKED" />
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

      <el-table 
        v-loading="loading"
        :data="userList" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" :label="$t('Username')" width="100" />
        <el-table-column prop="email" :label="$t('Email')" />
        <el-table-column prop="phone" :label="$t('Phone')" width="120"/>
        <el-table-column prop="role" :label="$t('Role')" width="150">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('users.status')" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('users.createTime')" width="200"/>
        <el-table-column :label="$t('common.actions')" width="400" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">{{ $t('common.edit') }}</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? $t('users.disable') : $t('users.enable') }}
            </el-button>
            <el-button size="small" type="info" @click="handleResetPassword(scope.row)">
              {{ $t('resetPassword') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 批量操作 -->
      <div v-if="selectedUsers.length > 0" class="batch-actions">
        <el-button type="danger" @click="handleBatchDelete">
          {{ $t('users.batchDelete') }} ({{ selectedUsers.length }})
        </el-button>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="isEdit ? $t('users.editUser') : $t('users.addUser')" width="600px" @close="handleDialogClose">
      <el-form :model="userForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('users.username')" prop="username">
              <el-input v-model="userForm.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('users.realName')" prop="realName">
              <el-input v-model="userForm.realName" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('users.email')" prop="email">
              <el-input v-model="userForm.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('users.phone')" prop="phone">
              <el-input v-model="userForm.phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('users.department')" prop="department">
              <el-input v-model="userForm.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('users.role')" prop="role">
              <el-select v-model="userForm.role" :placeholder="$t('users.selectRole')" style="width: 100%;">
                <el-option :label="$t('roles.admin')" value="ADMIN" />
                <el-option :label="$t('roles.developer')" value="DEVELOPER" />
                <el-option :label="$t('roles.viewer')" value="VIEWER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item v-if="!isEdit" :label="$t('users.password')" prop="password">
              <el-input v-model="userForm.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('users.status')" prop="status">
              <el-select v-model="userForm.status" :placeholder="$t('users.selectStatus')" style="width: 100%;">
                <el-option :label="$t('users.active')" value="ACTIVE" />
                <el-option :label="$t('users.inactive')" value="INACTIVE" />
                <el-option :label="$t('users.locked')" value="LOCKED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('users.remark')" prop="remark">
          <el-input v-model="userForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="showPasswordDialog" :title="$t('users.resetPassword')" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item :label="$t('newPassword')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('confirmPassword')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handlePasswordReset">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser, 
  deleteUsers, 
  toggleUserStatus, 
  resetUserPassword,
  checkUsername,
  checkEmail
} from '@/api/user'
import type { UserInfo } from '@/types/user'
import type { UserQueryParams, UserCreateRequest, UserUpdateRequest } from '@/api/user'

const { t } = useI18n()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  status: ''
})

// 用户表单
const userForm = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  role: '',
  status: 'ACTIVE',
  remark: ''
})

// 密码表单
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: t('users.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 50, message: t('users.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('users.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('users.passwordLength'), trigger: 'blur' }
  ],
  realName: [
    { required: true, message: t('users.realNameRequired'), trigger: 'blur' },
    { max: 50, message: t('users.realNameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('users.emailRequired'), trigger: 'blur' },
    { type: 'email' as const, message: t('users.emailInvalid'), trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('users.phoneInvalid'), trigger: 'blur' }
  ],
  role: [
    { required: true, message: t('users.roleRequired'), trigger: 'change' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: t('users.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('users.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('users.confirmPasswordRequired'), trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('users.passwordMismatch')))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 响应式数据
const userList = ref<UserInfo[]>([])
const selectedUsers = ref<UserInfo[]>([])
const loading = ref(false)
const saveLoading = ref(false)
const passwordLoading = ref(false)
const showAddDialog = ref(false)
const showPasswordDialog = ref(false)
const isEdit = ref(false)
const originalEmail = ref('')
const currentPasswordUserId = ref<number>()

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表单引用
const formRef = ref()
const passwordFormRef = ref()

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'DEVELOPER': return 'warning'
    case 'VIEWER': return 'info'
    default: return 'info'
  }
}

// 获取角色文本
const getRoleText = (role: string) => {
  switch (role) {
    case 'ADMIN': return t('roles.admin')
    case 'DEVELOPER': return t('roles.developer')
    case 'VIEWER': return t('roles.viewer')
    default: return role
  }
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'INACTIVE': return 'warning'
    case 'LOCKED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE': return t('users.active')
    case 'INACTIVE': return t('users.inactive')
    case 'LOCKED': return t('users.locked')
    default: return status
  }
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params: UserQueryParams = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      role: searchForm.role || undefined,
      status: searchForm.status || undefined
    }
    
    const result = await getUserList(params)
    if (result.success) {
      userList.value = result.data.records || result.data.list || []
      pagination.total = result.data.total
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    ElMessage.error(t('users.loadFailed'))
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
  pagination.page = 1
  loadUsers()
}

// 打开新增对话框
const handleAdd = () => {
  isEdit.value = false
  originalEmail.value = ''
  resetForm()
  showAddDialog.value = true
}

// 编辑用户
const handleEdit = (row: UserInfo) => {
  isEdit.value = true
  originalEmail.value = row.email
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    email: row.email,
    phone: row.phone,
    department: row.department,
    role: row.role,
    status: row.status,
    remark: row.remark
  })
  showAddDialog.value = true
}

// 切换用户状态
const handleToggleStatus = async (row: UserInfo) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? t('users.enable') : t('users.disable')
  
  try {
    await ElMessageBox.confirm(
      t('users.confirmToggleStatus', { action, username: row.username }),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const result = await toggleUserStatus(row.id, newStatus)
    if (result.success) {
      ElMessage.success(t('users.toggleStatusSuccess', { action }))
      loadUsers()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('users.toggleStatusFailed'))
      console.error('切换用户状态失败:', error)
    }
  }
}

// 重置密码
const handleResetPassword = (row: UserInfo) => {
  currentPasswordUserId.value = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  showPasswordDialog.value = true
}

// 执行密码重置
const handlePasswordReset = async () => {
  try {
    await passwordFormRef.value?.validate()
    
    passwordLoading.value = true
    const result = await resetUserPassword(currentPasswordUserId.value!, passwordForm.newPassword)
    if (result.success) {
      ElMessage.success(t('users.resetPasswordSuccess'))
      showPasswordDialog.value = false
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('重置密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 删除用户
const handleDelete = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      t('users.confirmDelete', { username: row.username }),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const result = await deleteUser(row.id)
    if (result.success) {
      ElMessage.success(t('users.deleteSuccess'))
      loadUsers()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('users.deleteFailed'))
      console.error('删除用户失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      t('users.confirmBatchDelete', { count: selectedUsers.value.length }),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    const ids = selectedUsers.value.map(user => user.id)
    const result = await deleteUsers(ids)
    if (result.success) {
      ElMessage.success(t('users.batchDeleteSuccess'))
      selectedUsers.value = []
      loadUsers()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('users.batchDeleteFailed'))
      console.error('批量删除用户失败:', error)
    }
  }
}

// 保存用户
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    
    saveLoading.value = true
    let result
    
    if (isEdit.value) {
      const updateData: UserUpdateRequest = {
        id: userForm.id!,
        realName: userForm.realName,
        email: userForm.email,
        phone: userForm.phone,
        department: userForm.department,
        role: userForm.role,
        status: userForm.status,
        remark: userForm.remark
      }
      result = await updateUser(updateData)
    } else {
      const createData: UserCreateRequest = {
        username: userForm.username,
        password: userForm.password,
        realName: userForm.realName,
        email: userForm.email,
        phone: userForm.phone,
        department: userForm.department,
        role: userForm.role,
        status: userForm.status,
        remark: userForm.remark
      }
      result = await createUser(createData)
    }
    
    if (result.success) {
      ElMessage.success(isEdit.value ? t('users.updateSuccess') : t('users.createSuccess'))
      handleDialogClose()
      loadUsers()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('保存用户失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  showAddDialog.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    department: '',
    role: '',
    status: 'ACTIVE',
    remark: ''
  })
  isEdit.value = false
  originalEmail.value = ''
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 选择变化
const handleSelectionChange = (selection: UserInfo[]) => {
  selectedUsers.value = selection
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadUsers()
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.batch-actions {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  border: 1px solid #e4e7ed;
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
  
  .batch-actions {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    margin-top: 20px;
    text-align: center;
  }
}
</style> 