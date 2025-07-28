<template>
  <div class="config-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">{{ $t('config.edit.title') }}</span>
          <div class="actions">
            <el-button @click="goBack" icon="ArrowLeft">{{ $t('common.back') }}</el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        v-loading="loading"
      >
        <el-form-item :label="$t('config.groupName')" prop="groupName">
          <el-input
            v-model="form.groupName"
            :placeholder="$t('config.placeholders.groupName')"
            disabled
          />
        </el-form-item>

        <el-form-item :label="$t('config.configKey')" prop="configKey">
          <el-input
            v-model="form.configKey"
            :placeholder="$t('config.placeholders.configKey')"
            disabled
          />
        </el-form-item>

        <el-form-item :label="$t('config.dataType')" prop="dataType">
          <el-select
            v-model="form.dataType"
            :placeholder="$t('config.placeholders.dataType')"
            disabled
          >
            <el-option label="String" value="String" />
            <el-option label="Number" value="Number" />
            <el-option label="Boolean" value="Boolean" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('config.configValue')" prop="configValue">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="6"
            :placeholder="$t('config.placeholders.configValue')"
            :show-password="form.encrypted"
          />
        </el-form-item>

        <el-form-item :label="$t('config.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('config.placeholders.description')"
          />
        </el-form-item>

        <el-form-item :label="$t('config.encrypted')" prop="encrypted">
          <el-switch
            v-model="form.encrypted"
            :active-text="$t('common.yes')"
            :inactive-text="$t('common.no')"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ $t('common.submit') }}
          </el-button>
          <el-button @click="goBack">{{ $t('common.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getConfigById, updateConfig } from '@/api/config'
import type { ConfigItem, ConfigForm } from '@/types/config'
import type { Ticket } from '@/types/ticket'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive<ConfigForm>({
  groupName: '',
  configKey: '',
  dataType: 'String',
  configValue: '',
  description: '',
  encrypted: false
})

const rules = {
  configValue: [
    { required: true, message: t('config.rules.configValueRequired'), trigger: 'blur' }
  ],
  description: [
    { max: 500, message: t('config.rules.descriptionMaxLength'), trigger: 'blur' }
  ]
}

// 方法
const loadConfigDetail = async () => {
  const configId = Number(route.params.id)
  if (!configId) {
    ElMessage.error(t('config.messages.configIdRequired'))
    return
  }

  loading.value = true
  try {
    const response = await getConfigById(configId)
    if (response.success) {
      const config = response.data
      Object.assign(form, {
        groupName: config.groupName,
        configKey: config.configKey,
        dataType: config.dataType,
        configValue: config.configValue,
        description: config.description,
        encrypted: config.encrypted
      })
    } else {
      ElMessage.error(response.message || t('config.messages.loadFailed'))
    }
  } catch (error) {
    console.error(t('config.messages.loadFailed'), error)
    ElMessage.error(t('config.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const configId = Number(route.params.id)
    const response = await updateConfig(configId, form)
    
    if (response.success) {
      ElMessage.success(t('config.messages.updateSuccess'))
      
      // 如果返回了ticket对象，跳转到ticket详情页面
      if (response.data && typeof response.data === 'object' && 'id' in response.data) {
        const ticket = response.data as Ticket
        router.push(`/ticket/detail/${ticket.id}`)
      } else {
        // 如果没有返回ticket，返回配置列表
        router.push('/config')
      }
    } else {
      ElMessage.error(response.message || t('config.messages.updateFailed'))
    }
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error(t('config.messages.updateFailed'))
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

// 生命周期
onMounted(() => {
  loadConfigDetail()
})
</script>

<style scoped lang="scss">
.config-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .title {
    font-size: 18px;
    font-weight: 600;
  }
  
  .actions {
    display: flex;
    gap: 10px;
  }
}
</style> 