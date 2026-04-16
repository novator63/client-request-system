<script setup>
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTicketDetails } from '../composables/useTicketDetails'
import { useComments } from '../composables/useComments'
import { useTicketHistory } from '../composables/useTicketHistory'
import { isClosedTicketStatus, TICKET_PRIORITIES, TICKET_STATUSES } from '../constants/ticket.constants'
import { formatDateTime, getPriorityLabel, getStatusLabel } from '../utils/ticketFormatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const {
  loading,
  updating,
  deleting,
  ticket,
  categories,
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
  loadTicket,
  enterEditMode,
  cancelEdit,
  saveChanges,
  closeTicket,
  deleteTicket,
} = useTicketDetails({ route, router, authStore })
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
            >
              Редактировать
            </el-button>
            <el-button v-if="editMode" @click="cancelEdit">Отмена</el-button>
            <el-button v-if="editMode" @click="saveChanges" type="primary" :loading="updating">
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
            <el-descriptions-item label="Статус">{{
              getStatusLabel(ticket.status)
            }}</el-descriptions-item>
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
                {{ isTicketClosed ? 'Только история комментариев' : 'Обсуждение заявки без перехода на отдельную страницу' }}
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

.comments-section {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
}

.history-section {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 16px;
  background: var(--el-bg-color);
  margin-bottom: 16px;
}

.history-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.history-section__header h3 {
  margin: 0;
  font-size: 16px;
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
  min-height: 72px;
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
}

.history-item__top-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.history-item__action {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.history-item__date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.history-item__meta {
  font-size: 13px;
  color: var(--el-text-color-regular);
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
}

.comments-section__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.comments-section__header h3 {
  margin: 0;
  font-size: 16px;
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
  min-height: 72px;
}

.comments-list__items {
  display: grid;
  gap: 12px;
}

.comment-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 14px 16px;
  background: var(--el-bg-color);
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
}

.comment-form {
  margin-top: 16px;
}

.comment-form__actions {
  display: flex;
  justify-content: flex-end;
}
</style>
