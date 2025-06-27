<template>
<<<<<<< HEAD
  <div class="profile-container">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>
      <div>个人中心页面 - 开发中...</div>
    </el-card>
=======
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="profile-info">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-button class="upload-btn" size="small" @click="handleAvatarUpload">
                更换头像
              </el-button>
            </div>
            <div class="info-section">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <label>邮箱：</label>
                <span>{{ userInfo.email }}</span>
              </div>
              <div class="info-item">
                <label>角色：</label>
                <el-tag :type="getRoleTagType(userInfo.role)">
                  {{ getRoleText(userInfo.role) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>注册时间：</label>
                <span>{{ userInfo.createTime }}</span>
              </div>
              <div class="info-item">
                <label>最后登录：</label>
                <span>{{ userInfo.lastLoginTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="修改信息" name="info">
            <el-card>
              <el-form :model="profileForm" label-width="100px" :rules="formRules" ref="formRef">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                <el-form-item label="部门" prop="department">
                  <el-input v-model="profileForm.department" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane label="修改密码" name="password">
            <el-card>
              <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          
          <el-tab-pane label="操作日志" name="logs">
            <el-card>
              <el-table :data="operationLogs" style="width: 100%">
                <el-table-column prop="operation" label="操作类型" />
                <el-table-column prop="target" label="操作对象" />
                <el-table-column prop="result" label="操作结果">
                  <template #default="scope">
                    <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
                      {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="ip" label="IP地址" />
                <el-table-column prop="createTime" label="操作时间" />
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
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
// 个人中心页面
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
}
=======
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

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
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const operationLogs = ref([
  {
    operation: '登录系统',
    target: '配置中心',
    result: 'SUCCESS',
    ip: '192.168.1.100',
    createTime: '2024-01-20 09:30:00'
  },
  {
    operation: '更新配置',
    target: 'user.service.timeout',
    result: 'SUCCESS',
    ip: '192.168.1.100',
    createTime: '2024-01-20 09:25:00'
  },
  {
    operation: '新增用户',
    target: 'testuser',
    result: 'SUCCESS',
    ip: '192.168.1.100',
    createTime: '2024-01-20 09:20:00'
  }
])

const logCurrentPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(50)

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
    case 'ADMIN': return '管理员'
    case 'DEVELOPER': return '开发者'
    case 'VIEWER': return '查看者'
    default: return role
  }
}

const handleAvatarUpload = () => {
  // TODO: 实现头像上传功能
  ElMessage.info('头像上传功能待实现')
}

const handleUpdateProfile = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      // TODO: 实现个人信息更新功能
      ElMessage.success('个人信息更新成功')
    }
  })
}

const handleChangePassword = () => {
  passwordFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // TODO: 实现密码修改功能
      ElMessage.success('密码修改成功')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  })
}

const handleLogSizeChange = (val: number) => {
  logPageSize.value = val
  // TODO: 重新加载日志数据
}

const handleLogCurrentChange = (val: number) => {
  logCurrentPage.value = val
  // TODO: 重新加载日志数据
}

onMounted(() => {
  // 初始化表单数据
  Object.assign(profileForm, {
    email: userInfo.email,
    phone: '',
    realName: '',
    department: ''
  })
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 20px;
}

.profile-info {
  text-align: center;
}

.avatar-section {
  margin-bottom: 30px;
  
  .upload-btn {
    margin-top: 15px;
  }
}

.info-section {
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  
  label {
    font-weight: bold;
    min-width: 80px;
    color: #606266;
  }
  
  span {
    color: #303133;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
</style> 