<template>
<<<<<<< HEAD
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">页面不存在</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被删除
      </div>
      <el-button type="primary" @click="goHome">
        <el-icon><House /></el-icon>
        返回首页
      </el-button>
=======
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-icon">
        <el-icon :size="120">
          <Warning />
        </el-icon>
      </div>
      <h1 class="not-found-title">404</h1>
      <p class="not-found-desc">抱歉，您访问的页面不存在</p>
      <div class="not-found-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
<<<<<<< HEAD
import { House } from '@element-plus/icons-vue'
=======
import { Warning, House, Back } from '@element-plus/icons-vue'
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
<<<<<<< HEAD
</script>

<style scoped lang="scss">
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
=======

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

<<<<<<< HEAD
.error-content {
=======
.not-found-content {
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
  text-align: center;
  color: white;
}

<<<<<<< HEAD
.error-code {
  font-size: 120px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.error-message {
  font-size: 32px;
  margin-bottom: 16px;
  font-weight: 500;
}

.error-description {
  font-size: 16px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.el-button {
  font-size: 16px;
  padding: 12px 24px;
=======
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
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
}
</style> 