<template>
  <div class="config-detail">
<<<<<<< HEAD
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置详情</span>
          <el-button @click="$router.go(-1)">返回</el-button>
        </div>
      </template>
      <div>配置详情页面 - 开发中...</div>
=======
    <el-page-header @back="goBack" content="配置详情" />
    
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>配置信息</span>
          <el-button type="primary" @click="handleEdit">编辑配置</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应用名称">{{ configDetail.appName }}</el-descriptions-item>
        <el-descriptions-item label="环境">{{ configDetail.env }}</el-descriptions-item>
        <el-descriptions-item label="配置组">{{ configDetail.configGroup }}</el-descriptions-item>
        <el-descriptions-item label="配置键">{{ configDetail.configKey }}</el-descriptions-item>
        <el-descriptions-item label="配置值" :span="2">
          <pre>{{ configDetail.configValue }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ configDetail.description }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ configDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ configDetail.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>变更历史</span>
      </template>
      
      <el-timeline>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="item.createTime"
        >
          <el-card>
            <h4>{{ item.operation }}</h4>
            <p>操作人：{{ item.operator }}</p>
            <p v-if="item.oldValue">旧值：{{ item.oldValue }}</p>
            <p v-if="item.newValue">新值：{{ item.newValue }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
    </el-card>
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
// 配置详情页面
=======
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const configDetail = ref({
  appName: '',
  env: '',
  configGroup: '',
  configKey: '',
  configValue: '',
  description: '',
  createTime: '',
  updateTime: ''
})

const historyList = ref([])

const goBack = () => {
  router.back()
}

const handleEdit = () => {
  // TODO: 实现编辑功能
  console.log('编辑配置')
}

onMounted(() => {
  const configId = route.params.id
  // TODO: 根据ID加载配置详情和历史记录
  console.log('加载配置详情:', configId)
})
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
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
<<<<<<< HEAD
=======

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
>>>>>>> 9e15f533479da8c5d591d9b69a4010f010642a43
</style> 