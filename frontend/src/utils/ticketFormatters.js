import { PRIORITY_LABELS, STATUS_LABELS } from '../constants/ticket.constants'

export const getStatusLabel = (status) => STATUS_LABELS[status] || status || '—'

export const getPriorityLabel = (priority) => PRIORITY_LABELS[priority] || priority || '—'

export const formatDateTime = (value) => {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('ru-RU')
}

export const getAssigneeLabel = (ticket) => {
  if (ticket?.assigneeName) {
    return ticket.assigneeName
  }

  if (ticket?.assigneeId) {
    return `ID: ${ticket.assigneeId}`
  }

  return 'Не назначен'
}
