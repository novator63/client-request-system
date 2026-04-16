import { PRIORITY_LABELS, STATUS_LABELS, STATUS_COLORS, STATUS_TAG_TYPE, PRIORITY_COLORS, PRIORITY_TAG_TYPE } from '../constants/ticket.constants'

export const getStatusLabel = (status) => STATUS_LABELS[status] || status || '—'

export const getStatusColor = (status) => STATUS_COLORS[status] || '#606266'

export const getStatusTagType = (status) => STATUS_TAG_TYPE[status] || 'info'

export const getPriorityLabel = (priority) => PRIORITY_LABELS[priority] || priority || '—'

export const getPriorityColor = (priority) => PRIORITY_COLORS[priority] || '#909399'

export const getPriorityTagType = (priority) => PRIORITY_TAG_TYPE[priority] || 'info'

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

/**
 * Safely convert value to integer, defaulting to 0
 */
export const normalizeCount = (value) => (Number.isFinite(Number(value)) ? Number(value) : 0)

/**
 * Normalize summary object with all required count fields
 */
export const normalizeSummary = (payload) => ({
  total: normalizeCount(payload?.total),
  open: normalizeCount(payload?.open),
  inProgress: normalizeCount(payload?.inProgress),
  closed: normalizeCount(payload?.closed),
  overdue: normalizeCount(payload?.overdue),
})

/**
 * Normalize array of report rows, ensuring count is numeric
 */
export const normalizeRows = (rows) =>
  rows.map((row) => ({
    ...row,
    count: normalizeCount(row?.count),
  }))
