<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTicketByIdApi,
  getCategoriesForTicketApi,
  updateTicketStatusApi,
  updateTicketClassificationApi,
  assignTicketApi,
  closeTicketApi
} from '../api/tickets.api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const updating = ref(false)
const ticket = ref(null)
const categories = ref([])
const notFound = ref(false)
const editMode = ref(false)

// Локальные копии для редактирования
const editStatus = ref(null)
const editPriority = ref(null)
const editCategory = ref(null)
const editAssigneeId = ref(null)

const ticketId = computed(() => route.params.id)
const isClient = computed(() => authStore.user?.role === 'CLIENT')
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const isOperator = computed(() => authStore.user?.role === 'OPERATOR')
const canEdit = computed(() => {
  const userRole = authStore.user?.role
  return userRole === 'ADMIN' || userRole === 'OPERATOR'
})

const TICKET_STATUSES = ['NEW', 'IN_PROGRESS', 'RESOLVED']
const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH']

const getStatusLabel = (status) => {
  const labels = {
    NEW: 'Новая',
    IN_PROGRESS: 'В работе',
    RESOLVED: 'Решена',
    CLOSED: 'Закрыта'
  }
  return labels[status] || status
}

const getPriorityLabel = (priority) => {
  const labels = {
    LOW: 'Низкий',
    MEDIUM: 'Средний',
    HIGH: 'Высокий'
  }
  return labels[priority] || priority
}

const loadTicket = async () => {
  loading.value = true
  notFound.value = false

  try {
    ticket.value = await getTicketByIdApi(ticketId.value)
    await loadCategories()
  } catch (error) {
    if (error.response?.status === 404) {
      notFound.value = true
      return
    }
    throw error
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getCategoriesForTicketApi()
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const enterEditMode = () => {
  editStatus.value = ticket.value.status
  editPriority.value = ticket.value.priority
  editCategory.value = ticket.value.categoryId
  editAssigneeId.value = ticket.value.assigneeId
  editMode.value = true
}

const cancelEdit = () => {
  editMode.value = false
  editStatus.value = null
  editPriority.value = null
  editCategory.value = null
  editAssigneeId.value = null
}

const saveChanges = async () => {
  updating.value = true

  try {
    // Обновляем статус если он изменился
    if (editStatus.value !== ticket.value.status) {
      await updateTicketStatusApi(ticketId.value, editStatus.value)
    }

    // Обновляем классификацию если изменилось
    if (isAdmin && (editCategory.value !== ticket.value.categoryId || editPriority.value !== ticket.value.priority)) {
      await updateTicketClassificationApi(ticketId.value, editCategory.value, editPriority.value)
    }

    // Обновляем ответственного если изменился
    if (isAdmin && editAssigneeId.value !== ticket.value.assigneeId) {
      await assignTicketApi(ticketId.value, editAssigneeId.value)
    }

    // Перезагружаем данные заявки
    await loadTicket()
    editMode.value = false
    ElMessage.success('Заявка успешно обновлена')
  } catch (error) {
    const errorMessage = error.response?.data?.message || 'Ошибка при обновлении заявки'
    ElMessage.error(errorMessage)
  } finally {
    updating.value = false
  }
}

const closeTicket = async () => {
  ElMessageBox.confirm('Вы уверены, что хотите закрыть эту заявку?', 'Подтверждение', {
    confirmButtonText: 'Закрыть',
    cancelButtonText: 'Отмена',
    type: 'warning'
  })
    .then(async () => {
      updating.value = true
      try {
        await closeTicketApi(ticketId.value)
        await loadTicket()
        ElMessage.success('Заявка успешно закрыта')
      } catch (error) {
        const errorMessage = error.response?.data?.message || 'Ошибка при закрытии заявки'
        ElMessage.error(errorMessage)
      } finally {
        updating.value = false
      }
    })
    .catch(() => {
      // User cancelled the action
    })
}

const formatDate = (value) => {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('ru-RU')
}

onMounted(loadTicket)
</script>

<template>
  <section>
    <el-card v-loading="loading">
      <template #header>
        <div class="details-header">
          <h1>Заявка #{{ ticketId }}</h1>
          <div class="header-actions">
                        <div style="font-size: 12px; color: #999; margin-right: 16px">
                          {{ authStore.user?.email || 'не авторизирован' }} | {{ authStore.user?.role || 'нет роли' }}
                        </div>
            <el-button
              v-if="canEdit && !editMode && ticket?.status !== 'CLOSED'"
              @click="enterEditMode"
              type="primary"
            >
              Редактировать
            </el-button>
            <el-button v-if="editMode" @click="cancelEdit">Отмена</el-button>
            <el-button v-if="editMode" @click="saveChanges" type="primary" :loading="updating">
              Сохранить
            </el-button>
            <el-button @click="router.push('/tickets')">К списку</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="notFound" description="Заявка не найдена" />

      <div v-else-if="ticket" class="ticket-details">
        <!-- Основная информация (не редактируется) -->
        <el-descriptions :column="1" border>
          <el-descriptions-item label="Тема">{{ ticket.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Описание">{{
            ticket.description || '—'
          }}</el-descriptions-item>
          <el-descriptions-item label="Автор">{{
            ticket.authorName || `ID: ${ticket.authorId || '—'}`
          }}</el-descriptions-item>
          <el-descriptions-item label="Создана">{{
            formatDate(ticket.createdAt)
          }}</el-descriptions-item>
          <el-descriptions-item label="Обновлена">{{
            formatDate(ticket.updatedAt)
          }}</el-descriptions-item>
        </el-descriptions>

        <!-- Редактируемые поля -->
        <div v-if="canEdit" class="editable-section">
          <div class="section-header">
            <h3>Параметры заявки</h3>
            <el-button
              v-if="ticket.status !== 'CLOSED'"
              type="danger"
              plain
              @click="closeTicket"
              :loading="updating"
            >
              Закрыть заявку
            </el-button>
          </div>

          <el-form label-width="120px">
            <!-- Статус (для всех) -->
            <el-form-item label="Статус">
              <template v-if="editMode">
                <el-select v-model="editStatus" placeholder="Выберите статус">
                  <el-option
                    v-for="status in TICKET_STATUSES"
                    :key="status"
                    :label="getStatusLabel(status)"
                    :value="status"
                  />
                </el-select>
              </template>
              <template v-else>
                <span>{{ getStatusLabel(ticket.status) }}</span>
              </template>
            </el-form-item>

            <!-- Приоритет и Категория (только для ADMIN) -->
            <template v-if="isAdmin">
              <el-form-item label="Приоритет">
                <template v-if="editMode">
                  <el-select v-model="editPriority" placeholder="Выберите приоритет">
                    <el-option
                      v-for="priority in TICKET_PRIORITIES"
                      :key="priority"
                      :label="getPriorityLabel(priority)"
                      :value="priority"
                    />
                  </el-select>
                </template>
                <template v-else>
                  <span>{{ getPriorityLabel(ticket.priority) || '—' }}</span>
                </template>
              </el-form-item>

              <el-form-item label="Категория">
                <template v-if="editMode">
                  <el-select v-model="editCategory" placeholder="Выберите категорию">
                    <el-option
                      v-for="cat in categories"
                      :key="cat.id"
                      :label="cat.name"
                      :value="cat.id"
                    />
                  </el-select>
                </template>
                <template v-else>
                  <span>{{ ticket.categoryName || '—' }}</span>
                </template>
              </el-form-item>

              <el-form-item label="Ответственный">
                <template v-if="editMode">
                  <el-input-number
                    v-model="editAssigneeId"
                    :min="0"
                    placeholder="ID пользователя (0 - не назначен)"
                  />
                </template>
                <template v-else>
                  <span>{{
                    ticket.assigneeName || (ticket.assigneeId ? `ID: ${ticket.assigneeId}` : 'Не назначен')
                  }}</span>
                </template>
              </el-form-item>
            </template>

            <!-- Срок (просмотр только) -->
            <el-form-item label="Срок">
              <span>{{ formatDate(ticket.dueAt) }}</span>
            </el-form-item>
          </el-form>
        </div>

        <!-- Для CLIENT: просто показываем статус без редактирования -->
        <div v-else class="status-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Статус">{{
              getStatusLabel(ticket.status)
            }}</el-descriptions-item>
            <el-descriptions-item label="Категория">{{
              ticket.categoryName || '—'
            }}</el-descriptions-item>
            <el-descriptions-item label="Срок">{{ formatDate(ticket.dueAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.details-header h1 {
  margin: 0;
  font-size: 24px;
  flex: 1;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.ticket-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.editable-section {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
  flex: 1;
}

.status-section {
  margin-top: 16px;
}
</style>
