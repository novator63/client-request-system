import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  assignTicketApi,
  closeTicketApi,
  getCategoriesForTicketApi,
  getTicketByIdApi,
  updateTicketClassificationApi,
  updateTicketStatusApi,
} from '../api/tickets.api'
import { USER_ROLES } from '../constants/ticket.constants'

export const useTicketDetails = ({ route, authStore }) => {
  const loading = ref(false)
  const updating = ref(false)
  const ticket = ref(null)
  const categories = ref([])
  const notFound = ref(false)
  const editMode = ref(false)

  const editStatus = ref(null)
  const editPriority = ref(null)
  const editCategory = ref(null)
  const editAssigneeId = ref(null)

  const ticketId = computed(() => route.params.id)
  const isAdmin = computed(() => authStore.user?.role === USER_ROLES.ADMIN)
  const isOperator = computed(() => authStore.user?.role === USER_ROLES.OPERATOR)
  const canEdit = computed(() => isAdmin.value || isOperator.value)

  const loadCategories = async () => {
    try {
      categories.value = await getCategoriesForTicketApi()
    } catch {
      categories.value = []
    }
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

  const enterEditMode = () => {
    if (!ticket.value) {
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
      ElMessage.success('Заявка успешно обновлена')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Ошибка при обновлении заявки')
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
      ElMessage.success('Заявка успешно закрыта')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Ошибка при закрытии заявки')
    } finally {
      updating.value = false
    }
  }

  return {
    loading,
    updating,
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
    loadTicket,
    enterEditMode,
    cancelEdit,
    saveChanges,
    closeTicket,
  }
}
