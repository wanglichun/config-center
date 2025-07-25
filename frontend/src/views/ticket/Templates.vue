<template>
  <div class="ticket-templates">
    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ t('ticket.templates.add') }}
          </el-button>
        </div>
        <div class="right">
          <el-button @click="loadData">
            <el-icon><Refresh /></el-icon>
            {{ t('common.refresh') }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 模板列表 -->
    <el-card>
      <template #header>
        <span>{{ t('ticket.templates.title') }}</span>
      </template>

      <el-table
        v-loading="loading"
        :data="templateList"
        style="width: 100%"
      >
        <el-table-column prop="id" :label="t('ticket.ticketId')" width="80" />
        <el-table-column prop="name" :label="t('ticket.templates.name')" min-width="150" />
        <el-table-column prop="type" :label="t('ticket.type')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ t(`ticket.types.${row.type}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" :label="t('ticket.priority')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ t(`ticket.priorities.${row.priority}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('ticket.templates.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createBy" :label="t('ticket.templates.createBy')" width="120" />
        <el-table-column prop="createTime" :label="t('ticket.createTime')" width="160" />
        <el-table-column :label="t('common.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleCreateFromTemplate(row)">
              {{ t('ticket.templates.createFromTemplate') }}
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              {{ t('ticket.templates.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              {{ t('ticket.templates.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('ticket.templates.edit') : t('ticket.templates.add')"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item :label="t('ticket.templates.name')" prop="name">
          <el-input v-model="form.name" :placeholder="'请输入模板名称'" />
        </el-form-item>
        <el-form-item :label="t('ticket.templates.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            :placeholder="'请输入模板描述'"
          />
        </el-form-item>
        <el-form-item :label="t('ticket.type')" prop="type">
          <el-select v-model="form.type" :placeholder="t('ticket.placeholders.type')" style="width: 100%">
            <el-option
              v-for="type in ticketTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.priority')" prop="priority">
          <el-select v-model="form.priority" :placeholder="t('ticket.placeholders.priority')" style="width: 100%">
            <el-option
              v-for="priority in ticketPriorities"
              :key="priority.value"
              :label="priority.label"
              :value="priority.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.tags')" prop="tags">
          <el-input
            v-model="form.tags"
            :placeholder="t('ticket.placeholders.tags')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ t('common.save') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 从模板创建工单对话框 -->
    <el-dialog
      v-model="createFromTemplateDialogVisible"
      :title="t('ticket.templates.createFromTemplate')"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item :label="t('ticket.ticketTitle')" prop="title">
          <el-input v-model="createForm.title" :placeholder="t('ticket.placeholders.title')" />
        </el-form-item>
        <el-form-item :label="t('ticket.description')" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            :placeholder="t('ticket.placeholders.description')"
          />
        </el-form-item>
        <el-form-item :label="t('ticket.assignee')" prop="assignee">
          <el-select v-model="createForm.assignee" :placeholder="t('ticket.placeholders.assignee')" style="width: 100%">
            <el-option label="developer1" value="developer1" />
            <el-option label="developer2" value="developer2" />
            <el-option label="developer3" value="developer3" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ticket.dueTime')" prop="dueTime">
          <el-date-picker
            v-model="createForm.dueTime"
            type="datetime"
            :placeholder="t('ticket.placeholders.dueTime')"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFromTemplateDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateFromTemplateSubmit" :loading="submitting">
          {{ t('common.create') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getTicketTemplates, createTicketTemplate, deleteTicketTemplate, createTicket } from '@/api/ticket'
import type { TicketTemplate, TicketForm } from '@/types/ticket'
import { TicketType, TicketPriority } from '@/types/ticket'

const { t } = useI18n()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const createFromTemplateDialogVisible = ref(false)
const isEdit = ref(false)
const templateList = ref<TicketTemplate[]>([])
const currentTemplate = ref<TicketTemplate | null>(null)

// 表单数据
const formRef = ref<FormInstance>()
const createFormRef = ref<FormInstance>()
const form = reactive({
  name: '',
  description: '',
  type: TicketType.BUG,
  priority: TicketPriority.MEDIUM,
  tags: []
})

const createForm = reactive<TicketForm>({
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
  name: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  type: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ],
  priority: [
    { required: true, message: t('validation.required'), trigger: 'change' }
  ]
}

const createRules: FormRules = {
  title: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
  ],
  description: [
    { required: true, message: t('validation.required'), trigger: 'blur' }
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
  loading.value = true
  try {
    templateList.value = await getTicketTemplates()
  } catch (error) {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    name: '',
    description: '',
    type: TicketType.BUG,
    priority: TicketPriority.MEDIUM,
    tags: []
  })
  dialogVisible.value = true
}

const handleEdit = (row: TicketTemplate) => {
  isEdit.value = true
  currentTemplate.value = row
  Object.assign(form, {
    name: row.name,
    description: row.description,
    type: row.type,
    priority: row.priority,
    tags: row.tags
  })
  dialogVisible.value = true
}

const handleDelete = async (row: TicketTemplate) => {
  try {
    await ElMessageBox.confirm(
      t('ticket.templates.deleteConfirm', { name: row.name }),
      t('common.confirm'),
      { type: 'warning' }
    )
    await deleteTicketTemplate(row.id)
    ElMessage.success(t('ticket.templates.deleteSuccess'))
    loadData()
  } catch (error) {
    // 用户取消或删除失败
  }
}

const handleCreateFromTemplate = (row: TicketTemplate) => {
  currentTemplate.value = row
  Object.assign(createForm, {
    title: '',
    description: row.description,
    type: row.type,
    priority: row.priority,
    assignee: '',
    dueTime: '',
    tags: row.tags,
    relatedConfigs: [],
    environment: '',
    appName: ''
  })
  createFromTemplateDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const templateData = {
      ...form,
      tags: form.tags ? form.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : []
    }

    if (isEdit.value && currentTemplate.value) {
      // TODO: 调用更新模板API
      ElMessage.success(t('ticket.templates.updateSuccess'))
    } else {
      await createTicketTemplate(templateData)
      ElMessage.success(t('ticket.templates.createSuccess'))
    }

    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const handleCreateFromTemplateSubmit = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()
    submitting.value = true

    const ticketData = {
      ...createForm,
      tags: createForm.tags ? createForm.tags.split(',').map(tag => tag.trim()).filter(tag => tag) : [],
      relatedConfigs: createForm.relatedConfigs ? createForm.relatedConfigs.split(',').map(id => Number(id.trim())).filter(id => !isNaN(id)) : []
    }

    await createTicket(ticketData)
    ElMessage.success(t('ticket.messages.createSuccess'))
    createFromTemplateDialogVisible.value = false
    router.push('/ticket')
  } catch (error) {
    ElMessage.error(t('ticket.messages.createFailed'))
  } finally {
    submitting.value = false
  }
}

// 工具方法
const getTypeTagType = (type: TicketType) => {
  const typeMap: Record<TicketType, string> = {
    [TicketType.BUG]: 'danger',
    [TicketType.FEATURE]: 'success',
    [TicketType.CONFIG]: 'warning',
    [TicketType.DEPLOY]: 'info',
    [TicketType.OTHER]: ''
  }
  return typeMap[type]
}

const getPriorityTagType = (priority: TicketPriority) => {
  const priorityMap: Record<TicketPriority, string> = {
    [TicketPriority.LOW]: 'info',
    [TicketPriority.MEDIUM]: 'warning',
    [TicketPriority.HIGH]: 'danger',
    [TicketPriority.URGENT]: 'danger'
  }
  return priorityMap[priority]
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ticket-templates {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  gap: 10px;
}

.right {
  display: flex;
  gap: 10px;
}
</style> 