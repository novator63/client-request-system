import { computed, ref, unref, watch } from 'vue'
import { createCommentByTicketIdApi, getCommentsByTicketIdApi } from '../api/comments.api'
import { getTicketByIdApi } from '../api/tickets.api'
import { isClosedTicketStatus } from '../constants/ticket.constants'
import { notifyApiError, notifySuccess, parseApiError } from '../utils/errorHandler'

export const useComments = ({ ticketId, ticketStatus }) => {
  const comments = ref([])
  const commentsLoading = ref(false)
  const commentsLoadError = ref('')
  const submittingComment = ref(false)
  const commentText = ref('')

  const currentTicketId = computed(() => unref(ticketId))
  const currentTicketStatus = computed(() => unref(ticketStatus))
  const canAddComments = computed(() => !isClosedTicketStatus(currentTicketStatus.value))

  const canSubmitComment = computed(() => {
    return (
      canAddComments.value &&
      !submittingComment.value &&
      !commentsLoading.value &&
      commentText.value.trim().length > 0
    )
  })

  const loadComments = async () => {
    const ticketIdValue = currentTicketId.value

    commentsLoadError.value = ''

    if (!ticketIdValue) {
      comments.value = []
      return
    }

    commentsLoading.value = true

    try {
      comments.value = await getCommentsByTicketIdApi(ticketIdValue)
    } catch (error) {
      if (error.response?.status !== 404) {
        commentsLoadError.value = parseApiError(error, {
          fallbackMessage: 'Не удалось загрузить комментарии',
        })
      }

      comments.value = []
    } finally {
      commentsLoading.value = false
    }
  }

  const submitComment = async () => {
    const ticketIdValue = currentTicketId.value
    const content = commentText.value.trim()

    if (!ticketIdValue || !content || submittingComment.value || !canAddComments.value) {
      return
    }

    submittingComment.value = true

    try {
      const latestTicket = await getTicketByIdApi(ticketIdValue)

      if (isClosedTicketStatus(latestTicket?.status)) {
        throw new Error('Заявка уже закрыта. Комментарии недоступны.')
      }

      const createdComment = await createCommentByTicketIdApi(ticketIdValue, { content })

      if (createdComment?.id) {
        comments.value = [...comments.value, createdComment]
      } else {
        await loadComments()
      }

      commentText.value = ''
      commentsLoadError.value = ''
      notifySuccess('Комментарий добавлен')
    } catch (error) {
      notifyApiError(error, {
        fallbackMessage: 'Не удалось добавить комментарий',
      })
    } finally {
      submittingComment.value = false
    }
  }

  watch(
    currentTicketId,
    () => {
      commentText.value = ''
      void loadComments()
    },
    { immediate: true },
  )

  return {
    comments,
    commentsLoading,
    commentsLoadError,
    submittingComment,
    commentText,
    canSubmitComment,
    canAddComments,
    loadComments,
    submitComment,
  }
}
