<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-icon">
        <el-icon :size="120">
          <Warning />
        </el-icon>
      </div>
      <h1 class="not-found-title">404</h1>
      <p class="not-found-desc">{{ $t('notFound.description') }}</p>
      <div class="not-found-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><House /></el-icon>
          {{ $t('notFound.backHome') }}
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          {{ $t('notFound.goBack') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Warning, House, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-content {
  text-align: center;
  color: white;
}

.not-found-icon {
  margin-bottom: 20px;
  opacity: 0.8;
}

.not-found-title {
  font-size: 72px;
  font-weight: bold;
  margin: 0 0 20px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.not-found-desc {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style> 