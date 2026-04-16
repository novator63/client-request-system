import { computed, ref, unref } from 'vue'
import { getTicketHistoryApi } from '../api/history.api'
import { HISTORY_ACTION_LABELS, USER_ROLES } from '../constants/ticket.constants'
import { parseApiError } from '../utils/errorHandler'

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
  let loadHistoryPromise = null

  const currentTicketId = computed(() => unref(ticketId))
  const currentUserRole = computed(() => authStore.user?.role)
  const canViewHistory = computed(() => {
    return currentUserRole.value === USER_ROLES.ADMIN || currentUserRole.value === USER_ROLES.OPERATOR
  })

  const loadHistory = async () => {
    if (loadHistoryPromise) {
      return loadHistoryPromise
    }

    const ticketIdValue = currentTicketId.value

    historyLoadError.value = ''

    if (!ticketIdValue || !canViewHistory.value) {
      historyEntries.value = []
      return
    }

    loadHistoryPromise = (async () => {
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
    })()

    try {
      await loadHistoryPromise
    } finally {
      loadHistoryPromise = null
    }
  }

  return {
    historyEntries,
    historyLoading,
    historyLoadError,
    canViewHistory,
    loadHistory,
  }
}
