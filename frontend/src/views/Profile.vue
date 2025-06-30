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
                <label>{{ $t('profile.email') }}：</label>
                <span>{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.role') }}：</label>
                <el-tag :type="getRoleTagType(userInfo.role)">
                  {{ getRoleText(userInfo.role) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.registerTime') }}：</label>
                <span>{{ userInfo.createTime }}</span>
              </div>
              <div class="info-item">
                <label>{{ $t('profile.lastLoginTime') }}：</label>
                <span>{{ userInfo.lastLoginTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="$t('profile.modifyInfo')" name="info">
            <el-card>
              <el-form :model="profileForm" label-width="100px" :rules="formRules" ref="formRef">
                <el-form-item :label="$t('profile.email')" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item :label="$t('profile.phone')" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item :label="$t('profile.realName')" prop="realName">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                <el-form-item :label="$t('profile.department')" prop="department">
                  <el-input v-model="profileForm.department" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateProfile">{{ $t('profile.saveChanges') }}</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane :label="$t('profile.changePassword')" name="password">
            <el-card>
              <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
                <el-form-item :label="$t('profile.currentPassword')" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">{{ $t('profile.changePassword') }}</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane :label="$t('profile.operationLogs')" name="logs">
            <el-card>
              <el-table :data="operationLogs" style="width: 100%">
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
                <el-table-column prop="createTime" :label="$t('profile.operationTime')" />
              </el-table>
              
              <div class="pagination">
                <el-pagination
                  v-model:current-page="logCurrentPage"
                  v-model:page-size="logPageSize"
                  :page-sizes="[10, 20, 50]"
                  :total="logTotal"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()
const activeTab = ref('info')

const userInfo = reactive({
  username: 'admin',
  email: 'admin@example.com',
  role: 'ADMIN',
  avatar: '',
  createTime: '2024-01-01 10:00:00',
  lastLoginTime: '2024-01-20 09:30:00'
})

const profileForm = reactive({
  email: '',
  phone: '',
  realName: '',
  department: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  email: [
    { required: true, message: t('profile.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('profile.emailInvalid'), trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: t('profile.currentPasswordRequired'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('profile.newPasswordRequired'), trigger: 'blur' },
    { min: 6, message: t('profile.passwordMinLength'), trigger: 'blur' }
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

const operationLogs = ref([
  { operation: t('profile.login'), target: t('profile.system'), result: 'SUCCESS', ip: '192.168.1.100', createTime: '2024-01-20 09:30:00' },
  { operation: t('profile.modifyConfig'), target: 'user.timeout', result: 'SUCCESS', ip: '192.168.1.100', createTime: '2024-01-20 09:25:00' }
])

const logCurrentPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(2)
const formRef = ref()
const passwordFormRef = ref()

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

const handleAvatarUpload = () => {
  ElMessage.info(t('profile.uploadTodoMessage'))
}

const handleUpdateProfile = () => {
  formRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success(t('profile.updateSuccess'))
    }
  })
}

const handleChangePassword = () => {
  passwordFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success(t('profile.passwordChangeSuccess'))
    }
  })
}

const handleLogSizeChange = (val: number) => {
  logPageSize.value = val
}

const handleLogCurrentChange = (val: number) => {
  logCurrentPage.value = val
}

onMounted(() => {
  // 初始化用户信息
  Object.assign(profileForm, userInfo)
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
      margin-bottom: 12px;
      
      label {
        width: 80px;
        font-weight: 500;
        color: #666;
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 