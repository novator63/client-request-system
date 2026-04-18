import { computed, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import {
  assignTicketApi,
  closeTicketApi,
  deleteTicketApi,
  getCategoriesForTicketApi,
  getTicketByIdApi,
  updateTicketClassificationApi,
  updateTicketStatusApi,
} from '../api/tickets.api'
import { getAvailableOperatorsApi } from '../api/users.api'
import { isClosedTicketStatus, USER_ROLES } from '../constants/ticket.constants'
import { notifyApiError, notifySuccess, parseApiError } from '../utils/errorHandler'

export const useTicketDetails = ({ route, router, authStore }) => {
  const loading = ref(false)
  const updating = ref(false)
  const deleting = ref(false)
  const ticket = ref(null)
  const categories = ref([])
  const operators = ref([])
  const operatorsLoading = ref(false)
  const operatorsLoadError = ref('')
  const notFound = ref(false)
  const editMode = ref(false)

  const editStatus = ref(null)
  const editPriority = ref(null)
  const editCategory = ref(null)
  const editAssigneeId = ref(null)
  let loadTicketPromise = null
  let categoriesLoaded = false
  let operatorsLoaded = false

  const ticketId = computed(() => route.params.id)
  const isAdmin = computed(() => authStore.user?.role === USER_ROLES.ADMIN)
  const isOperator = computed(() => authStore.user?.role === USER_ROLES.OPERATOR)
  const canEdit = computed(() => isAdmin.value || isOperator.value)
  const ticketAuthorLabel = computed(() => {
    if (!ticket.value) {
      return '—'
    }

    return ticket.value.authorName || `ID: ${ticket.value.authorId || '—'}`
  })
  const ticketAssigneeLabel = computed(() => {
    if (!ticket.value) {
      return '—'
    }

    if (ticket.value.assigneeName) {
      return ticket.value.assigneeName
    }

    if (ticket.value.assigneeId) {
      const selectedOperator = operators.value.find((operator) => operator.id === ticket.value.assigneeId)
      if (selectedOperator?.fullName) {
        return selectedOperator.fullName
      }

      return `ID: ${ticket.value.assigneeId}`
    }

    return 'Не назначен'
  })

  const selectedEditAssigneeLabel = computed(() => {
    if (editAssigneeId.value == null) {
      return 'Не назначен'
    }

    const selectedOperator = operators.value.find((operator) => operator.id === editAssigneeId.value)
    if (selectedOperator?.fullName && selectedOperator?.email) {
      return `${selectedOperator.fullName} (${selectedOperator.email})`
    }

    if (selectedOperator?.fullName) {
      return selectedOperator.fullName
    }

    return `ID: ${editAssigneeId.value}`
  })

  const loadCategories = async () => {
    if (categoriesLoaded) {
      return
    }

    try {
      categories.value = await getCategoriesForTicketApi()
      categoriesLoaded = true
    } catch {
      categories.value = []
      categoriesLoaded = false
    }
  }

  const toNumberOrZero = (value) => {
    const numericValue = Number(value)
    return Number.isFinite(numericValue) ? numericValue : 0
  }

  const sortOperatorsByWorkload = (items = []) => {
    return [...items].sort((left, right) => {
      const leftOverdue = toNumberOrZero(left?.overdueTicketsCount)
      const rightOverdue = toNumberOrZero(right?.overdueTicketsCount)
      if (leftOverdue !== rightOverdue) {
        return leftOverdue - rightOverdue
      }

      const leftActive = toNumberOrZero(left?.activeTicketsCount)
      const rightActive = toNumberOrZero(right?.activeTicketsCount)
      if (leftActive !== rightActive) {
        return leftActive - rightActive
      }

      return String(left?.fullName || '').localeCompare(String(right?.fullName || ''), 'ru')
    })
  }

  const loadOperators = async () => {
    if (operatorsLoaded || !isAdmin.value) {
      return
    }

    operatorsLoading.value = true
    operatorsLoadError.value = ''

    try {
      operators.value = sortOperatorsByWorkload(await getAvailableOperatorsApi())
      operatorsLoaded = true
    } catch (error) {
      operators.value = []
      operatorsLoaded = false
      operatorsLoadError.value = parseApiError(error, {
        fallbackMessage: 'Не удалось загрузить список операторов',
      })
    } finally {
      operatorsLoading.value = false
    }
  }

  const reloadOperators = async () => {
    operatorsLoaded = false
    await loadOperators()
  }

  const loadTicket = async () => {
    if (loadTicketPromise) {
      return loadTicketPromise
    }

    loadTicketPromise = (async () => {
      loading.value = true
      notFound.value = false

      try {
        ticket.value = await getTicketByIdApi(ticketId.value)

        if (isAdmin.value) {
          await Promise.all([loadCategories(), loadOperators()])
        }
      } catch (error) {
        if (error.response?.status === 404) {
          ticket.value = null
          notFound.value = true
          return
        }

        ticket.value = null
        notifyApiError(error, {
          fallbackMessage: 'Не удалось загрузить заявку',
        })
      } finally {
        loading.value = false
      }
    })()

    try {
      await loadTicketPromise
    } finally {
      loadTicketPromise = null
    }
  }

  const enterEditMode = () => {
    if (!ticket.value) {
      return
    }

    if (isClosedTicketStatus(ticket.value.status) && !isAdmin.value) {
      return
    }

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
    if (!ticket.value) {
      return
    }

    updating.value = true
    const assigneeChanged = isAdmin.value && editAssigneeId.value !== ticket.value.assigneeId
    const assigneeSuccessLabel = selectedEditAssigneeLabel.value

    try {
      if (editStatus.value !== ticket.value.status) {
        await updateTicketStatusApi(ticketId.value, editStatus.value)
      }

      if (
        isAdmin.value &&
        (editCategory.value !== ticket.value.categoryId || editPriority.value !== ticket.value.priority)
      ) {
        await updateTicketClassificationApi(ticketId.value, editCategory.value, editPriority.value)
      }

      if (isAdmin.value && editAssigneeId.value !== ticket.value.assigneeId) {
        await assignTicketApi(ticketId.value, editAssigneeId.value)
      }

      await loadTicket()
      editMode.value = false
      notifySuccess(
        assigneeChanged
          ? `Ответственный выбран: ${assigneeSuccessLabel}`
          : 'Заявка успешно обновлена',
      )
    } catch (error) {
      notifyApiError(error, {
        fallbackMessage: 'Ошибка при обновлении заявки',
      })
    } finally {
      updating.value = false
    }
  }

  const closeTicket = async () => {
    try {
      await ElMessageBox.confirm('Вы уверены, что хотите закрыть эту заявку?', 'Подтверждение', {
        confirmButtonText: 'Закрыть',
        cancelButtonText: 'Отмена',
        type: 'warning',
      })
    } catch {
      return
    }

    updating.value = true

    try {
      await closeTicketApi(ticketId.value)
      await loadTicket()
      notifySuccess('Заявка успешно закрыта')
    } catch (error) {
      notifyApiError(error, {
        fallbackMessage: 'Ошибка при закрытии заявки',
      })
    } finally {
      updating.value = false
    }
  }

  const deleteTicket = async () => {
    if (!ticket.value || !isAdmin.value) {
      return
    }

    try {
      await ElMessageBox.confirm(
        'Вы уверены, что хотите удалить эту заявку? Действие необратимо.',
        'Подтверждение удаления',
        {
          confirmButtonText: 'Удалить',
          cancelButtonText: 'Отмена',
          type: 'warning',
          confirmButtonClass: 'el-button--danger',
        },
      )
    } catch {
      return
    }

    deleting.value = true

    try {
      await deleteTicketApi(ticketId.value)
      notifySuccess('Заявка успешно удалена')
      await router.push('/tickets')
    } catch (error) {
      notifyApiError(error, {
        fallbackMessage: 'Не удалось удалить заявку',
      })
    } finally {
      deleting.value = false
    }
  }

  return {
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
  }
}
