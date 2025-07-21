<template>
  <div class="user-email-display">
    <el-card class="email-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Message /></el-icon>
            用户邮箱信息
          </span>
          <el-button @click="refreshEmail" size="small" icon="Refresh">
            刷新
          </el-button>
        </div>
      </template>
      
      <div class="email-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="Cookie中的邮箱">
            <el-tag v-if="cookieEmail" type="success" size="large">
              {{ cookieEmail }}
            </el-tag>
            <el-tag v-else type="info" size="large">
              未设置
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="Store中的邮箱">
            <el-tag v-if="storeEmail" type="primary" size="large">
              {{ storeEmail }}
            </el-tag>
            <el-tag v-else type="info" size="large">
              未设置
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="邮箱状态">
            <el-tag v-if="isEmailMatch" type="success">
              一致
            </el-tag>
            <el-tag v-else-if="cookieEmail && storeEmail" type="warning">
              不一致
            </el-tag>
            <el-tag v-else type="info">
              未登录
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="actions">
          <el-button @click="copyEmail" :disabled="!cookieEmail" type="primary">
            复制邮箱
          </el-button>
          <el-button @click="clearEmail" :disabled="!cookieEmail" type="danger">
            清除邮箱
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserEmail, removeUserEmail } from '@/utils/cookie'

const userStore = useUserStore()

const cookieEmail = ref<string | undefined>('')
const storeEmail = ref<string | undefined>('')

// 计算邮箱是否一致
const isEmailMatch = computed(() => {
  return cookieEmail.value === storeEmail.value && cookieEmail.value !== undefined
})

// 刷新邮箱信息
const refreshEmail = () => {
  cookieEmail.value = getUserEmail()
  storeEmail.value = userStore.userInfo?.email
  ElMessage.success('邮箱信息已刷新')
}

// 复制邮箱
const copyEmail = async () => {
  if (!cookieEmail.value) return
  
  try {
    await navigator.clipboard.writeText(cookieEmail.value)
    ElMessage.success('邮箱已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 清除邮箱
const clearEmail = () => {
  removeUserEmail()
  cookieEmail.value = undefined
  ElMessage.success('邮箱已从cookie中清除')
}

// 组件挂载时获取邮箱信息
onMounted(() => {
  refreshEmail()
})
</script>

<style scoped lang="scss">
.user-email-display {
  .email-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
      }
    }
    
    .email-content {
      .actions {
        margin-top: 16px;
        display: flex;
        gap: 12px;
        justify-content: center;
      }
    }
  }
}
</style> 