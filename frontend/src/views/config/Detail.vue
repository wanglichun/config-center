<template>
  <div class="config-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置详情</span>
          <el-button @click="$router.go(-1)">返回</el-button>
        </div>
      </template>
      <div>配置详情页面 - 开发中...</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// 配置详情页面
</script>

<style scoped lang="scss">
.config-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 