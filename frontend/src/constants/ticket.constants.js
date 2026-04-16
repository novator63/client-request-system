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
