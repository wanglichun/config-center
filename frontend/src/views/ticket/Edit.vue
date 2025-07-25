<template>
  <div class="ticket-edit">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            {{ t('common.back') }}
          </el-button>
          <span>{{ isEdit ? t('ticket.edit') : t('ticket.add') }}</span>
        </div>
      </template>

      <div v-loading="loading">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="ticket-form"
        >
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item :label="t('ticket.ticketTitle')" prop="title">
                <el-input
                  v-model="form.title"
                  :placeholder="t('ticket.placeholders.title')"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item :label="t('ticket.description')" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="6"
                  :placeholder="t('ticket.placeholders.description')"
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item :label="t('ticket.type')" prop="type">
                    <el-select
                      v-model="form.type"
                      :placeholder="t('ticket.placeholders.type')"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="type in ticketTypes"
                        :key="type.value"
                        :label="type.label"
                        :value="type.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="t('ticket.priority')" prop="priority">
                    <el-select
                      v-model="form.priority"
                      :placeholder="t('ticket.placeholders.priority')"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="priority in ticketPriorities"
                        :key="priority.value"
                        :label="priority.label"
                        :value="priority.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item :label="t('ticket.assignee')" prop="assignee">
                    <el-select
                      v-model="form.assignee"
                      :placeholder="t('ticket.placeholders.assignee')"
                      style="width: 100%"
                      clearable
                    >
                      <el-option label="developer1" value="developer1" />
                      <el-option label="developer2" value="developer2" />
                      <el-option label="developer3" value="developer3" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="t('ticket.dueTime')" prop="dueTime">
                    <el-date-picker
                      v-model="form.dueTime"
                      type="datetime"
                      :placeholder="t('ticket.placeholders.dueTime')"
                      style="width: 100%"
                      clearable
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item :label="t('ticket.environment')" prop="environment">
                    <el-select
                      v-model="form.environment"
                      placeholder="请选择环境"
                      style="width: 100%"
                      clearable
                    >
                      <el-option label="开发环境" value="development" />
                      <el-option label="测试环境" value="test" />
                      <el-option label="生产环境" value="production" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="t('ticket.appName')" prop="appName">
                    <el-input
                      v-model="form.appName"
                      placeholder="请输入应用名称"
                      clearable
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item :label="t('ticket.tags')" prop="tags">
                <el-input
                  v-model="form.tags"
                  :placeholder="t('ticket.placeholders.tags')"
                  clearable
                />
                <div class="form-tip">多个标签用逗号分隔</div>
              </el-form-item>

              <el-form-item :label="t('ticket.relatedConfigs')" prop="relatedConfigs">
                <el-input
                  v-model="form.relatedConfigs"
                  placeholder="请输入关联配置ID，多个用逗号分隔"
                  clearable
                />
                <div class="form-tip">关联的配置项ID，多个用逗号分隔</div>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <div class="form-sidebar">
                <h4>工单信息</h4>
                <div class="info-item">
                  <span class="label">工单编号：</span>
                  <span class="value">{{ isEdit ? ticketId : '新建' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">创建人：</span>
                  <span class="value">当前用户</span>
                </div>
                <div class="info-item">
                  <span class="label">创建时间：</span>
                  <span class="value">{{ isEdit ? ticket?.createTime : '待创建' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">状态：</span>
                  <span class="value">
                    <el-tag v-if="isEdit" :type="getStatusTagType(ticket?.status || TicketStatus.PENDING)">
                      {{ t(`ticket.statuses.${ticket?.status || TicketStatus.PENDING}`) }}
                    </el-tag>
                    <span v-else>待创建</span>
                  </span>
                </div>

                <div class="form-actions">
                  <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                    {{ t('common.save') }}
                  </el-button>
                  <el-button @click="goBack" size="large">
                    {{ t('common.cancel') }}
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getTicketDetail, createTicket, updateTicket } from '@/api/ticket'
import type { Ticket, TicketForm } from '@/types/ticket'
import { TicketType, TicketPriority, TicketStatus } from '@/types/ticket'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const ticket = ref<Ticket | null>(null)
const ticketId = computed(() => Number(route.params.id))
const isEdit = computed(() => !!ticketId.value)

// 表单数据
const formRef = ref<FormInstance>()
const form = reactive<TicketForm>({
  title: '',
  description: '',
  type: TicketType.BUG,
  priority: TicketPriority.MEDIUM,
  assignee: '',
  dueTime: '',
  tags: [],
  relatedConfigs: [],
  environment: '',
  appName: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { min: 2, max: 200, message: t('validation.minLength', { min: 2 }), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.required'), trigger: 'blur' },
    { min: 10, max: 1000, message: t('validation.minLength', { min: 10 }), trigger: 'blur' }
  ],
  type: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ],
  priority: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ]
}

// 计算属性
const ticketTypes = computed(() => [
  { value: TicketType.BUG, label: t('ticket.types.BUG') },
  { value: TicketType.FEATURE, label: t('ticket.types.FEATURE') },
  { value: TicketType.CONFIG, label: t('ticket.types.CONFIG') },
  { value: TicketType.DEPLOY, label: t('ticket.types.DEPLOY') },
  { value: TicketType.OTHER, label: t('ticket.types.OTHER') }
])

const ticketPriorities = computed(() => [
  { value: TicketPriority.LOW, label: t('ticket.priorities.LOW') },
  { value: TicketPriority.MEDIUM, label: t('ticket.priorities.MEDIUM') },
  { value: TicketPriority.HIGH, label: t('ticket.priorities.HIGH') },
  { value: TicketPriority.URGENT, label: t('ticket.priorities.URGENT') }
])

// 方法
const loadData = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    ticket.value = await getTicketDetail(ticketId.value)
    Object.assign(form, {
      title: ticket.value.title,
      description: ticket.value.description,
      type: ticket.value.type,
      priority: ticket.value.priority,
      assignee: ticket.value.assignee,
      dueTime: ticket.value.dueTime,
      tags: ticket.value.tags,
      relatedConfigs: ticket.value.relatedConfigs,
      environment: ticket.value.environment,
      appName: ticket.value.appName
    })
  } catch (error) {
    ElMessage.error(t('ticket.messages.loadFailed'))
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 处理标签和关联配置
    const processedForm = {
      ...form,
      tags: form.tags ? form.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : [],
      relatedConfigs: form.relatedConfigs ? form.relatedConfigs.split(',').map(id => Number(id.trim())).filter(id => !isNaN(id)) : []
    }

    if (isEdit.value) {
      await updateTicket(ticketId.value, processedForm)
      ElMessage.success(t('ticket.messages.updateSuccess'))
    } else {
      await createTicket(processedForm)
      ElMessage.success(t('ticket.messages.createSuccess'))
    }

    goBack()
  } catch (error) {
    ElMessage.error(isEdit.value ? t('ticket.messages.updateFailed') : t('ticket.messages.createFailed'))
  } finally {
    submitting.value = false
  }
}

// 工具方法
const getStatusTagType = (status?: TicketStatus) => {
  if (!status) return 'warning'
  const statusMap: Record<TicketStatus, string> = {
    [TicketStatus.PENDING]: 'warning',
    [TicketStatus.IN_PROGRESS]: 'primary',
    [TicketStatus.RESOLVED]: 'success',
    [TicketStatus.CLOSED]: 'info',
    [TicketStatus.REJECTED]: 'danger'
  }
  return statusMap[status]
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ticket-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ticket-form {
  margin-top: 20px;
}

.form-sidebar {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}

.form-sidebar h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 