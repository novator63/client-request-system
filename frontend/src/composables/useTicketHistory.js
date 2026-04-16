import { computed, ref, unref, watch } from 'vue'
import { getTicketHistoryApi } from '../api/history.api'
import { USER_ROLES } from '../constants/ticket.constants'
import { parseApiError } from '../utils/errorHandler'

const HISTORY_ACTION_LABELS = {
  TICKET_CREATED: 'Создана',
  ASSIGNED: 'Назначена',
  STATUS_CHANGED: 'Изменен статус',
  CATEGORY_CHANGED: 'Изменена категория',
  CLOSED: 'Закрыта',
  COMMENT_ADDED: 'Добавлен комментарий',
}

const normalizeHistoryEntry = (entry) => {
  const actionType = entry?.actionType || ''

  return {
    id: entry?.id,
    actionType,
    actionLabel: HISTORY_ACTION_LABELS[actionType] || actionType || 'Событие',
    description: entry?.description || '',
    actorId: entry?.actorId,
    actorFullName: entry?.actorFullName,
    createdAt: entry?.createdAt,
  }
}

export const useTicketHistory = ({ ticketId, authStore }) => {
  const historyEntries = ref([])
  const historyLoading = ref(false)
  const historyLoadError = ref('')

  const currentTicketId = computed(() => unref(ticketId))
  const currentUserRole = computed(() => authStore.user?.role)
  const canViewHistory = computed(() => {
    return currentUserRole.value === USER_ROLES.ADMIN || currentUserRole.value === USER_ROLES.OPERATOR
  })

  const loadHistory = async () => {
    const ticketIdValue = currentTicketId.value

    historyLoadError.value = ''

    if (!ticketIdValue || !canViewHistory.value) {
      historyEntries.value = []
      return
    }

    historyLoading.value = true

    try {
      const response = await getTicketHistoryApi(ticketIdValue)
      historyEntries.value = response.entries.map(normalizeHistoryEntry)
    } catch (error) {
      historyEntries.value = []
      historyLoadError.value = parseApiError(error, {
        fallbackMessage: 'Не удалось загрузить историю действий',
      })
    } finally {
      historyLoading.value = false
    }
  }

  watch(
    [currentTicketId, canViewHistory],
    () => {
      void loadHistory()
    },
    { immediate: true },
  )

  return {
    historyEntries,
    historyLoading,
    historyLoadError,
    canViewHistory,
    loadHistory,
  }
}
