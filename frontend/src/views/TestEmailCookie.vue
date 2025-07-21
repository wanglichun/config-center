<template>
  <div class="test-email-cookie">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span class="title">用户邮箱Cookie测试</span>
        </div>
      </template>
      
      <div class="test-content">
        <el-alert
          title="功能说明"
          type="info"
          :closable="false"
          show-icon
        >
          <p>此页面用于测试用户邮箱Cookie功能：</p>
          <ul>
            <li>用户登录后，邮箱会自动保存到Cookie中（key: USER_EMAIL）</li>
            <li>Cookie有效期为7天</li>
            <li>用户登出时，Cookie会被自动清除</li>
            <li>支持安全设置（HTTPS环境使用secure，防止CSRF攻击）</li>
          </ul>
        </el-alert>
        
        <div class="test-section">
          <h3>当前状态</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="登录状态">
              <el-tag :type="isLoggedIn ? 'success' : 'danger'">
                {{ isLoggedIn ? '已登录' : '未登录' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户邮箱">
              <span>{{ userEmail || '未设置' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="Cookie邮箱">
              <span>{{ cookieEmail || '未设置' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="Cookie状态">
              <el-tag :type="hasCookieEmail ? 'success' : 'info'">
                {{ hasCookieEmail ? '存在' : '不存在' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="test-section">
          <h3>操作测试</h3>
          <div class="test-actions">
            <el-button @click="refreshStatus" type="primary">
              刷新状态
            </el-button>
            <el-button @click="showAllCookies" type="info">
              查看所有Cookie
            </el-button>
            <el-button @click="clearEmailCookie" type="warning" :disabled="!hasCookieEmail">
              清除邮箱Cookie
            </el-button>
            <el-button @click="setTestEmail" type="success">
              设置测试邮箱
            </el-button>
          </div>
        </div>
        
        <div class="test-section" v-if="allCookies">
          <h3>所有Cookie信息</h3>
          <el-table :data="cookieList" border>
            <el-table-column prop="key" label="Cookie Key" />
            <el-table-column prop="value" label="Cookie Value" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserEmail, removeUserEmail, setUserEmail, getAllCookies } from '@/utils/cookie'

const userStore = useUserStore()

const userEmail = ref<string>('')
const cookieEmail = ref<string>('')
const allCookies = ref<Record<string, string>>({})

// 计算属性
const isLoggedIn = computed(() => !!userStore.token)
const hasCookieEmail = computed(() => !!cookieEmail.value)
const cookieList = computed(() => {
  return Object.entries(allCookies.value).map(([key, value]) => ({
    key,
    value: value.length > 50 ? value.substring(0, 50) + '...' : value
  }))
})

// 刷新状态
const refreshStatus = () => {
  userEmail.value = userStore.userInfo?.email || ''
  cookieEmail.value = getUserEmail() || ''
  ElMessage.success('状态已刷新')
}

// 显示所有Cookie
const showAllCookies = () => {
  allCookies.value = getAllCookies()
  ElMessage.success('已加载所有Cookie信息')
}

// 清除邮箱Cookie
const clearEmailCookie = () => {
  removeUserEmail()
  cookieEmail.value = ''
  ElMessage.success('邮箱Cookie已清除')
}

// 设置测试邮箱
const setTestEmail = () => {
  const testEmail = 'test@example.com'
  setUserEmail(testEmail)
  cookieEmail.value = testEmail
  ElMessage.success(`测试邮箱已设置: ${testEmail}`)
}

// 组件挂载时初始化
onMounted(() => {
  refreshStatus()
})
</script>

<style scoped lang="scss">
.test-email-cookie {
  padding: 20px;
  
  .test-card {
    .card-header {
      .title {
        font-weight: 600;
        font-size: 16px;
      }
    }
    
    .test-content {
      .test-section {
        margin-top: 20px;
        
        h3 {
          margin-bottom: 16px;
          color: #303133;
        }
        
        .test-actions {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
        }
      }
    }
  }
}
</style> 