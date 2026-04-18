<script setup>
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTicketDetails } from '../composables/useTicketDetails'
import { useComments } from '../composables/useComments'
import { useTicketHistory } from '../composables/useTicketHistory'
import { isClosedTicketStatus, TICKET_PRIORITIES, TICKET_STATUSES } from '../constants/ticket.constants'
import { formatDateTime, getPriorityLabel, getPriorityTagType, getStatusLabel, getStatusTagType } from '../utils/ticketFormatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const {
  loading,
  updating,
  deleting,
  ticket,
  categories,
  operators,
  operatorsLoading,
  operatorsLoadError,
  notFound,
  editMode,
  editStatus,
  editPriority,
  editCategory,
  editAssigneeId,
  ticketId,
  isAdmin,
  canEdit,
  ticketAuthorLabel,
  ticketAssigneeLabel,
  selectedEditAssigneeLabel,
  loadTicket,
  reloadOperators,
  enterEditMode,
  cancelEdit,
  saveChanges,
  closeTicket,
  deleteTicket,
} = useTicketDetails({ route, router, authStore })

const formatOperatorOptionLabel = (operator) => {
  const activeTicketsCount = Number(operator?.activeTicketsCount) || 0
  const overdueTicketsCount = Number(operator?.overdueTicketsCount) || 0

  return `${operator.fullName} (${operator.email}) — активных: ${activeTicketsCount}, просроченных: ${overdueTicketsCount}`
}

const ticketStatus = computed(() => ticket.value?.status)
const isTicketClosed = computed(() => isClosedTicketStatus(ticketStatus.value))
const {
  comments,
  commentsLoading,
  commentsLoadError,
  submittingComment,
  commentText,
  canSubmitComment,
  loadComments,
  submitComment,
  canAddComments,
} = useComments({ ticketId, ticketStatus })
const {
  historyEntries,
  historyLoading,
  historyLoadError,
  canViewHistory,
  loadHistory,
} = useTicketHistory({ ticketId, authStore })

let syncTimerId = null
let isSyncInProgress = false
let queuedSync = false

const performDataSync = async () => {
  await loadTicket()
  await Promise.all([loadComments(), loadHistory()])
}

const syncTicketState = async () => {
  if (isSyncInProgress) {
    queuedSync = true
    return
  }

  isSyncInProgress = true

  try {
    do {
      queuedSync = false
      await performDataSync()
    } while (queuedSync)
  } finally {
    isSyncInProgress = false
  }
}

watch(
  ticketId,
  () => {
    cancelEdit()
    void syncTicketState()
  },
  { immediate: true },
)

onMounted(() => {
  syncTimerId = window.setInterval(() => {
    void syncTicketState()
  }, 15000)
})

onBeforeUnmount(() => {
  if (syncTimerId) {
    window.clearInterval(syncTimerId)
    syncTimerId = null
  }
})
</script>

<template>
  <section>
    <el-card v-loading="loading">
      <template #header>
        <div class="details-header">
          <h1>Заявка #{{ ticketId }}</h1>
          <div class="header-actions">
            <el-button
              v-if="canEdit && !editMode && (!isTicketClosed || isAdmin)"
              @click="enterEditMode"
              type="primary"
              :disabled="deleting"
            >
              Редактировать
            </el-button>
            <el-button v-if="editMode" @click="cancelEdit" :disabled="updating || deleting">Отмена</el-button>
            <el-button v-if="editMode" @click="saveChanges" type="primary" :loading="updating" :disabled="deleting">
              Сохранить
            </el-button>
            <el-button
              v-if="editMode && isAdmin"
              @click="deleteTicket"
              type="danger"
              plain
              :loading="deleting"
              :disabled="updating"
            >
              Удалить
            </el-button>
            <el-button @click="router.push('/tickets')" :disabled="deleting">К списку</el-button>
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
          <el-descriptions-item label="Автор">{{ ticketAuthorLabel }}</el-descriptions-item>
          <el-descriptions-item label="Создана">{{
            formatDateTime(ticket.createdAt)
          }}</el-descriptions-item>
          <el-descriptions-item label="Обновлена">{{
            formatDateTime(ticket.updatedAt)
          }}</el-descriptions-item>
        </el-descriptions>

        <!-- Редактируемые поля -->
        <div v-if="canEdit" class="editable-section">
          <div class="section-header">
            <h3>Параметры заявки</h3>
            <el-button
              v-if="!isTicketClosed"
              type="danger"
              plain
              @click="closeTicket"
              :loading="updating"
              :disabled="editMode || deleting"
            >
              Закрыть заявку
            </el-button>
          </div>

          <el-form label-width="120px">
            <!-- Статус (для всех) -->
            <el-form-item label="Статус">
              <template v-if="editMode">
                <el-select v-model="editStatus" placeholder="Выберите статус" :disabled="deleting">
                  <el-option
                    v-for="status in TICKET_STATUSES"
                    :key="status"
                    :label="getStatusLabel(status)"
                    :value="status"
                  />
                </el-select>
              </template>
              <template v-else>
                <el-tag :type="getStatusTagType(ticket.status)" size="small">
                  {{ getStatusLabel(ticket.status) }}
                </el-tag>
              </template>
            </el-form-item>

            <!-- Приоритет и Категория (только для ADMIN) -->
            <template v-if="isAdmin">
              <el-form-item label="Приоритет">
                <template v-if="editMode">
                  <el-select v-model="editPriority" placeholder="Выберите приоритет" :disabled="deleting">
                    <el-option
                      v-for="priority in TICKET_PRIORITIES"
                      :key="priority"
                      :label="getPriorityLabel(priority)"
                      :value="priority"
                    />
                  </el-select>
                </template>
                <template v-else>
                  <el-tag v-if="ticket.priority" :type="getPriorityTagType(ticket.priority)" size="small">
                    {{ getPriorityLabel(ticket.priority) }}
                  </el-tag>
                  <span v-else>—</span>
                </template>
              </el-form-item>

              <el-form-item label="Категория">
                <template v-if="editMode">
                  <el-select v-model="editCategory" placeholder="Выберите категорию" :disabled="deleting">
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
                  <div class="assignee-field">
                    <el-select
                      v-model="editAssigneeId"
                      placeholder="Выберите оператора"
                      :loading="operatorsLoading"
                      :disabled="deleting || operatorsLoading"
                      :clearable="ticket.assigneeId === null"
                    >
                      <el-option
                        :label="'Не назначен'"
                        :value="null"
                        :disabled="ticket.assigneeId !== null"
                      />
                      <el-option
                        v-for="operator in operators"
                        :key="operator.id"
                        :label="formatOperatorOptionLabel(operator)"
                        :value="operator.id"
                      />
                    </el-select>
                    <el-alert
                      v-if="operatorsLoadError"
                      :title="operatorsLoadError"
                      type="error"
                      show-icon
                      :closable="false"
                    />
                    <el-button
                      v-if="operatorsLoadError"
                      text
                      size="small"
                      :loading="operatorsLoading"
                      :disabled="deleting"
                      @click="reloadOperators"
                    >
                      Повторить загрузку
                    </el-button>
                    <span class="assignee-selected-hint">Выбрано: {{ selectedEditAssigneeLabel }}</span>
                  </div>
                </template>
                <template v-else>
                  <span>{{ ticketAssigneeLabel }}</span>
                </template>
              </el-form-item>
            </template>

            <!-- Срок (просмотр только) -->
            <el-form-item label="Срок">
              <span>{{ formatDateTime(ticket.dueAt) }}</span>
            </el-form-item>
          </el-form>
        </div>

        <!-- Для CLIENT: просто показываем статус без редактирования -->
        <div v-else class="status-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Статус">
              <el-tag :type="getStatusTagType(ticket.status)" size="small">
                {{ getStatusLabel(ticket.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Категория">{{
              ticket.categoryName || '—'
            }}</el-descriptions-item>
            <el-descriptions-item label="Срок">{{ formatDateTime(ticket.dueAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="comments-section">
          <div v-if="canViewHistory" class="history-section">
            <div class="history-section__header">
              <div>
                <h3>История действий</h3>
                <p>Лента изменений по заявке с указанием исполнителя и времени</p>
              </div>

              <el-button text :loading="historyLoading" @click="loadHistory">Обновить</el-button>
            </div>

            <el-alert
              v-if="historyLoadError"
              :title="historyLoadError"
              type="error"
              show-icon
              :closable="false"
              class="history-section__alert"
            />

            <div class="history-list" v-loading="historyLoading">
              <el-empty
                v-if="!historyLoading && historyEntries.length === 0 && !historyLoadError"
                description="История действий пока пуста"
              />

              <div v-else-if="historyEntries.length > 0" class="history-list__items">
                <article
                  v-for="entry in historyEntries"
                  :key="entry.id || `${entry.actionType}-${entry.createdAt}`"
                  class="history-item"
                >
                  <div class="history-item__top-row">
                    <div class="history-item__action">{{ entry.actionLabel }}</div>
                    <div class="history-item__date">{{ formatDateTime(entry.createdAt) }}</div>
                  </div>

                  <div class="history-item__meta">
                    Выполнил:
                    <span>{{ entry.actorFullName || `ID: ${entry.actorId || '—'}` }}</span>
                  </div>

                  <p v-if="entry.description" class="history-item__description">
                    {{ entry.description }}
                  </p>
                </article>
              </div>
            </div>
          </div>

          <div class="comments-section__header">
            <div>
              <h3>Комментарии</h3>
              <p>
                {{ isTicketClosed ? 'Заявка закрыта. Новые комментарии добавлять нельзя.' : 'Обсуждение заявки без перехода на отдельную страницу' }}
              </p>
            </div>

            <el-button text :loading="commentsLoading" @click="loadComments">Обновить</el-button>
          </div>

          <el-alert
            v-if="commentsLoadError"
            :title="commentsLoadError"
            type="error"
            show-icon
            :closable="false"
            class="comments-section__alert"
          />

          <div class="comments-list" v-loading="commentsLoading">
            <el-empty
              v-if="!commentsLoading && comments.length === 0 && !commentsLoadError"
              description="Комментариев пока нет"
            />

            <div v-else-if="comments.length > 0" class="comments-list__items">
              <article v-for="comment in comments" :key="comment.id" class="comment-card">
                <div class="comment-card__header">
                  <div class="comment-card__author">
                    {{ comment.authorFullName || `ID: ${comment.authorId || '—'}` }}
                  </div>
                  <div class="comment-card__date">{{ formatDateTime(comment.createdAt) }}</div>
                </div>

                <p class="comment-card__content">{{ comment.content }}</p>
              </article>
            </div>
          </div>

          <el-form v-if="canAddComments" class="comment-form" @submit.prevent="submitComment">
            <el-form-item label="Новый комментарий">
              <el-input
                v-model="commentText"
                type="textarea"
                :rows="4"
                :maxlength="500"
                show-word-limit
                placeholder="Напишите комментарий и нажмите отправить"
                :disabled="submittingComment"
              />
            </el-form-item>

            <div class="comment-form__actions">
              <el-button
                type="primary"
                native-type="submit"
                :loading="submittingComment"
                :disabled="!canSubmitComment"
              >
                Отправить
              </el-button>
            </div>
          </el-form>
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
  gap: 16px;
  flex-wrap: wrap;
}

.details-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  flex: 1;
  min-width: 250px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ticket-details {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.editable-section {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 20px;
  background-color: var(--el-fill-color-light);
}

.assignee-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.assignee-selected-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  flex: 1;
  min-width: 150px;
}

.status-section {
  margin-top: 12px;
}

.comments-section {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 20px;
  background-color: var(--el-fill-color-light);
}

.history-section {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 20px;
  background: var(--el-bg-color);
  margin-bottom: 20px;
}

.history-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.history-section__header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.history-section__header p {
  margin: 4px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.history-section__alert {
  margin-bottom: 16px;
}

.history-list {
  min-height: 80px;
}

.history-list__items {
  display: grid;
  gap: 12px;
}

.history-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 14px 16px;
  background: var(--el-fill-color-extra-light);
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: var(--el-fill-color-light);
}

.history-item__top-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.history-item__action {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.history-item__date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.history-item__meta {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.history-item__meta span {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.history-item__description {
  margin: 8px 0 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  font-size: 13px;
}

.comments-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.comments-section__header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.comments-section__header p {
  margin: 4px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.comments-section__alert {
  margin-bottom: 16px;
}

.comments-list {
  min-height: 80px;
}

.comments-list__items {
  display: grid;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 14px 16px;
  background: var(--el-bg-color);
  transition: box-shadow 0.2s;
}

.comment-card:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.comment-card__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-card__author {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.comment-card__date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.comment-card__content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--el-text-color-primary);
  line-height: 1.6;
  font-size: 13px;
}

.comment-form {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.comment-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
</style>
