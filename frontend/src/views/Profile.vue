<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>{{ $t('profile.personalInfo') }}</span>
          </template>
          <div class="profile-info">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-button class="upload-btn" size="small" @click="handleAvatarUpload">
                {{ $t('profile.changeAvatar') }}
              </el-button>
            </div>
            <div class="info-section">
              <div class="info-item">
                <label>{{ $t('profile.username') }}：</label>
                <span>{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.realName') }}：</label>
                <span>{{ userInfo.realName }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.email') }}：</label>
                <span>{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.phone') }}：</label>
                <span>{{ userInfo.phone || '-' }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.department') }}：</label>
                <span>{{ userInfo.department || '-' }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.role') }}：</label>
                <el-tag :type="getRoleTagType(userInfo.role)">
                  {{ getRoleText(userInfo.role) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.status') }}：</label>
                <el-tag :type="getStatusTagType(userInfo.status)">
                  {{ getStatusText(userInfo.status) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.registerTime') }}：</label>
                <span>{{ userInfo.createTime }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.lastLoginTime') }}：</label>
                <span>{{ formatLastLoginTime(userInfo.lastLoginTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="$t('profile.modifyInfo')" name="info">
            <el-card>
              <el-form 
                v-loading="updateLoading"
                :model="profileForm" 
                label-width="100px" 
                :rules="formRules" 
                ref="formRef"
              >
                <el-form-item :label="$t('profile.realName')" prop="realName">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                <el-form-item :label="$t('profile.email')" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item :label="$t('profile.phone')" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item :label="$t('profile.department')" prop="department">
                  <el-input v-model="profileForm.department" />
                </el-form-item>
                <el-form-item :label="$t('profile.remark')" prop="remark">
                  <el-input 
                    v-model="profileForm.remark" 
                    type="textarea" 
                    :rows="3"
                    :placeholder="$t('profile.remarkPlaceholder')"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :loading="updateLoading"
                    @click="handleUpdateProfile"
                  >
                    {{ $t('profile.save') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane label="邮箱Cookie信息" name="email">
            <UserEmailDisplay />
          </el-tab-pane>
          
          <el-tab-pane :label="$t('profile.changePassword')" name="password">
            <el-card>
              <el-form 
                v-loading="passwordLoading"
                :model="passwordForm" 
                label-width="100px" 
                :rules="passwordRules" 
                ref="passwordFormRef"
              >
                <el-form-item :label="$t('profile.currentPassword')" prop="oldPassword">
                  <el-input 
                    v-model="passwordForm.oldPassword" 
                    type="password" 
                    show-password 
                    autocomplete="off"
                  />
                </el-form-item>
                <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    type="password" 
                    show-password 
                    autocomplete="off"
                  />
                </el-form-item>
                <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    type="password" 
                    show-password 
                    autocomplete="off"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary" 
                    :loading="passwordLoading"
                    @click="handleChangePassword"
                  >
                    {{ $t('profile.changePassword') }}
                  </el-button>
                  <el-button @click="resetPasswordForm">
                    {{ $t('common.reset') }}
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane :label="$t('profile.operationLogs')" name="logs">
            <el-card>
              <div class="log-search">
                <el-form :model="logSearchForm" inline>
                  <el-form-item :label="$t('profile.operationType')">
                    <el-select v-model="logSearchForm.operation" clearable style="width: 150px;">
                      <el-option :label="$t('profile.login')" value="LOGIN" />
                      <el-option :label="$t('profile.logout')" value="LOGOUT" />
                      <el-option :label="$t('profile.modifyInfo')" value="UPDATE_PROFILE" />
                      <el-option :label="$t('profile.changePassword')" value="CHANGE_PASSWORD" />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="loadOperationLogs">
                      {{ $t('common.search') }}
                    </el-button>
                    <el-button @click="resetLogSearch">
                      {{ $t('common.reset') }}
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>

              <el-table v-loading="logLoading" :data="operationLogs" style="width: 100%">
                <el-table-column prop="operation" :label="$t('profile.operationType')" />
                <el-table-column prop="target" :label="$t('profile.operationTarget')" />
                <el-table-column prop="result" :label="$t('profile.operationResult')">
                  <template #default="scope">
                    <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
                      {{ scope.row.result === 'SUCCESS' ? $t('profile.success') : $t('profile.failed') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="ip" :label="$t('profile.ipAddress')" />
                <el-table-column prop="userAgent" :label="$t('profile.userAgent')" show-overflow-tooltip />
                <el-table-column prop="createTime" :label="$t('profile.operationTime')" />
              </el-table>
              
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="logPagination.page"
                  v-model:page-size="logPagination.size"
                  :page-sizes="[10, 20, 50]"
                  :total="logPagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleLogSizeChange"
                  @current-change="handleLogCurrentChange"
                />
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarDialog" :title="$t('profile.changeAvatar')" width="400px">
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="handleAvatarRequest"
        >
          <img v-if="newAvatarUrl" :src="newAvatarUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tips">
          <p>{{ $t('profile.avatarTips') }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="avatarLoading" @click="handleSaveAvatar">
          {{ $t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { 
  getCurrentUserProfile, 
  updateCurrentUserProfile, 
  changePassword 
} from '@/api/user'
import type { UserInfo } from '@/types/user'
import type { UserUpdateRequest, PasswordChangeRequest } from '@/api/user'
import UserEmailDisplay from '@/components/UserEmailDisplay.vue'

const { t } = useI18n()
const userStore = useUserStore()
const activeTab = ref('info')

// 响应式数据
const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  role: '',
  status: '',
  avatar: '',
  remark: '',
  createTime: '',
  lastLoginTime: ''
})

const profileForm = reactive({
  realName: '',
  email: '',
  phone: '',
  department: '',
  remark: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const logSearchForm = reactive({
  operation: ''
})

const logPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载状态
const updateLoading = ref(false)
const passwordLoading = ref(false)
const logLoading = ref(false)
const avatarLoading = ref(false)
const showAvatarDialog = ref(false)
const newAvatarUrl = ref('')

// 表单引用
const formRef = ref()
const passwordFormRef = ref()

// 操作日志
const operationLogs = ref<any[]>([])

// 表单验证规则
const formRules = {
  realName: [
    { required: true, message: t('profile.realNameRequired'), trigger: 'blur' },
    { max: 50, message: t('profile.realNameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('profile.emailRequired'), trigger: 'blur' },
    { type: 'email' as const, message: t('profile.emailInvalid'), trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('profile.phoneInvalid'), trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: t('profile.currentPasswordRequired'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('profile.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('profile.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 工具函数
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

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'INACTIVE': return 'warning'
    case 'LOCKED': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE': return t('users.active')
    case 'INACTIVE': return t('users.inactive')
    case 'LOCKED': return t('users.locked')
    default: return status
  }
}

const formatLastLoginTime = (timestamp: string | number | undefined) => {
  if (!timestamp) return '-'
  try {
    if (typeof timestamp === 'number') {
      return new Date(timestamp).toLocaleString()
    }
    return timestamp
  } catch (error) {
    return '-'
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const result = await getCurrentUserProfile()
    if (result.success) {
      userInfo.value = result.data
      // 初始化表单数据
      Object.assign(profileForm, {
        realName: result.data.realName || '',
        email: result.data.email || '',
        phone: result.data.phone || '',
        department: result.data.department || '',
        remark: result.data.remark || ''
      })
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
    ElMessage.error(t('profile.loadUserInfoFailed'))
  }
}

// 更新用户信息
const handleUpdateProfile = async () => {
  try {
    await formRef.value?.validate()
    
    updateLoading.value = true
    const updateData: UserUpdateRequest = {
      id: userInfo.value.id,
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone,
      department: profileForm.department,
      role: userInfo.value.role, // 保持原有角色
      status: userInfo.value.status, // 保持原有状态
      remark: profileForm.remark
    }
    
    const result = await updateCurrentUserProfile(updateData)
    if (result.success) {
      ElMessage.success(t('profile.updateSuccess'))
      // 更新本地用户信息
      Object.assign(userInfo.value, result.data)
      // 更新用户store
      userStore.userInfo = result.data
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('更新用户信息失败:', error)
  } finally {
    updateLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    
    passwordLoading.value = true
    const passwordData: PasswordChangeRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    }
    
    const result = await changePassword(passwordData)
    if (result.success) {
      ElMessage.success(t('profile.passwordChangeSuccess'))
      resetPasswordForm()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 重置表单
const resetProfileForm = () => {
  Object.assign(profileForm, {
    realName: userInfo.value.realName || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    department: userInfo.value.department || '',
    remark: userInfo.value.remark || ''
  })
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  nextTick(() => {
    passwordFormRef.value?.clearValidate()
  })
}

// 头像相关
const handleAvatarUpload = () => {
  newAvatarUrl.value = userInfo.value.avatar || ''
  showAvatarDialog.value = true
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error(t('profile.avatarFormatError'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('profile.avatarSizeError'))
    return false
  }
  return true
}

const handleAvatarRequest = (options: any) => {
  const file = options.file
  const reader = new FileReader()
  reader.onload = (e) => {
    newAvatarUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const handleSaveAvatar = async () => {
  try {
    avatarLoading.value = true
    // 这里应该先上传图片到服务器，然后更新用户信息
    // 为了简化，直接使用base64
    const updateData: UserUpdateRequest = {
      id: userInfo.value.id,
      realName: userInfo.value.realName,
      email: userInfo.value.email,
      phone: userInfo.value.phone,
      department: userInfo.value.department,
      role: userInfo.value.role,
      status: userInfo.value.status,
      avatar: newAvatarUrl.value,
      remark: userInfo.value.remark
    }
    
    const result = await updateCurrentUserProfile(updateData)
    if (result.success) {
      userInfo.value.avatar = newAvatarUrl.value
      showAvatarDialog.value = false
      ElMessage.success(t('profile.avatarUpdateSuccess'))
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    console.error('更新头像失败:', error)
    ElMessage.error(t('profile.avatarUpdateFailed'))
  } finally {
    avatarLoading.value = false
  }
}

// 操作日志相关
const loadOperationLogs = async () => {
  logLoading.value = true
  try {
    // 模拟加载操作日志
    // 实际应该调用API获取用户操作日志
    setTimeout(() => {
      operationLogs.value = [
        { 
          operation: t('profile.login'), 
          target: t('profile.system'), 
          result: 'SUCCESS', 
          ip: '192.168.1.100', 
          userAgent: 'Chrome/91.0',
          createTime: '2024-01-20 09:30:00' 
        },
        { 
          operation: t('profile.modifyInfo'), 
          target: t('profile.personalInfo'), 
          result: 'SUCCESS', 
          ip: '192.168.1.100', 
          userAgent: 'Chrome/91.0',
          createTime: '2024-01-20 09:25:00' 
        }
      ]
      logPagination.total = operationLogs.value.length
      logLoading.value = false
    }, 1000)
  } catch (error: any) {
    console.error('加载操作日志失败:', error)
    logLoading.value = false
  }
}

const resetLogSearch = () => {
  logSearchForm.operation = ''
  logPagination.page = 1
  loadOperationLogs()
}

const handleLogSizeChange = (size: number) => {
  logPagination.size = size
  logPagination.page = 1
  loadOperationLogs()
}

const handleLogCurrentChange = (page: number) => {
  logPagination.page = page
  loadOperationLogs()
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserInfo()
  loadOperationLogs()
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 20px;
}

.profile-info {
  .avatar-section {
    text-align: center;
    margin-bottom: 20px;
    
    .upload-btn {
      margin-top: 10px;
    }
  }
  
  .info-section {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      label {
        width: 100px;
        font-weight: 500;
        color: #666;
        flex-shrink: 0;
      }
      
      span {
        flex: 1;
      }
    }
  }
}

.log-search {
  margin-bottom: 20px;
  
  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    align-items: flex-end;
    
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.avatar-upload {
  text-align: center;
  
  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
  
  .upload-tips {
    margin-top: 10px;
    color: #999;
    font-size: 12px;
    
    p {
      margin: 0;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .el-row {
    flex-direction: column;
    
    .el-col {
      width: 100% !important;
      margin-bottom: 20px;
    }
  }
  
  .log-search .el-form {
    flex-direction: column;
    align-items: stretch;
    
    .el-form-item {
      width: 100%;
    }
  }
}
</style> 