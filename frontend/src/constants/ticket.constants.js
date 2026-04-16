export const USER_ROLES = {
  ADMIN: 'ADMIN',
  OPERATOR: 'OPERATOR',
  CLIENT: 'CLIENT',
}

export const TICKET_STATUSES = ['NEW', 'IN_PROGRESS', 'RESOLVED']
export const CLOSED_TICKET_STATUS = 'CLOSED'

export const isClosedTicketStatus = (status) => status === CLOSED_TICKET_STATUS

export const STATUS_OPTIONS = [
  { label: 'Новый', value: 'NEW' },
  { label: 'В работе', value: 'IN_PROGRESS' },
  { label: 'Решен', value: 'RESOLVED' },
  { label: 'Закрыт', value: CLOSED_TICKET_STATUS },
]

export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH']

export const PRIORITY_OPTIONS = [
  { label: 'Низкий', value: 'LOW' },
  { label: 'Средний', value: 'MEDIUM' },
  { label: 'Высокий', value: 'HIGH' },
]

export const STATUS_LABELS = {
  NEW: 'Новая',
  IN_PROGRESS: 'В работе',
  RESOLVED: 'Решена',
  CLOSED: 'Закрыта',
}

export const PRIORITY_LABELS = {
  LOW: 'Низкий',
  MEDIUM: 'Средний',
  HIGH: 'Высокий',
}

export const HISTORY_ACTION_LABELS = {
  TICKET_CREATED: 'Создана',
  ASSIGNED: 'Назначена',
  STATUS_CHANGED: 'Изменен статус',
  CATEGORY_CHANGED: 'Изменена категория',
  CLOSED: 'Закрыта',
  COMMENT_ADDED: 'Добавлен комментарий',
}

/**
 * Status colors for el-tag component
 */
export const STATUS_COLORS = {
  NEW: '#409eff',
  IN_PROGRESS: '#e6a23c',
  RESOLVED: '#67c23a',
  CLOSED: '#909399',
}

export const STATUS_TAG_TYPE = {
  NEW: 'info',
  IN_PROGRESS: 'warning',
  RESOLVED: 'success',
  CLOSED: 'info',
}

/**
 * Priority colors for el-tag component
 */
export const PRIORITY_COLORS = {
  LOW: '#909399',
  MEDIUM: '#e6a23c',
  HIGH: '#f56c6c',
}

export const PRIORITY_TAG_TYPE = {
  LOW: 'info',
  MEDIUM: 'warning',
  HIGH: 'danger',
}
