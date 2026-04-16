import { ElMessage } from 'element-plus'

const DEFAULT_NETWORK_MESSAGE = 'Ошибка сети. Проверьте подключение и попробуйте снова.'

const getValidationMessages = (error) => {
  const validationErrors = error?.response?.data?.validationErrors

  if (!Array.isArray(validationErrors) || validationErrors.length === 0) {
    return []
  }

  return validationErrors
    .map((item) => item?.message)
    .filter((message) => typeof message === 'string' && message.trim().length > 0)
}

export const isNetworkError = (error) => {
  return Boolean(
    error?.code === 'ERR_NETWORK' ||
      error?.message === 'Network Error' ||
      (!error?.response && error?.request),
  )
}

export const parseApiError = (error, options = {}) => {
  const {
    fallbackMessage = 'Произошла ошибка. Попробуйте позже.',
    networkMessage = DEFAULT_NETWORK_MESSAGE,
  } = options

  const validationMessages = getValidationMessages(error)

  if (validationMessages.length > 0) {
    return validationMessages.join('; ')
  }

  if (isNetworkError(error)) {
    return networkMessage
  }

  const apiMessage = error?.response?.data?.message

  if (typeof apiMessage === 'string' && apiMessage.trim().length > 0) {
    return apiMessage
  }

  if (typeof error?.message === 'string' && error.message.trim().length > 0) {
    return error.message
  }

  return fallbackMessage
}

export const notifyApiError = (error, options = {}) => {
  const message = parseApiError(error, options)
  ElMessage.error(message)
  return message
}

export const notifySuccess = (message) => {
  if (!message) {
    return
  }

  ElMessage.success(message)
}
