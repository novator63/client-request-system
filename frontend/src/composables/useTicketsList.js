import { computed, ref } from 'vue'
import { getTicketsApi } from '../api/tickets.api'
import { USER_ROLES } from '../constants/ticket.constants'

export const useTicketsList = ({ authStore }) => {
  const loading = ref(false)
  const tickets = ref([])

  const filters = ref({
    status: '',
    priority: '',
    category: '',
    query: '',
  })

  const page = ref(1)
  const pageSize = ref(10)

  const isClient = computed(() => authStore.user?.role === USER_ROLES.CLIENT)
  const isOperator = computed(() => authStore.user?.role === USER_ROLES.OPERATOR)

  const roleScopedTickets = computed(() => {
    if (!isOperator.value) {
      return tickets.value
    }

    const currentUserId = authStore.user?.id
    return tickets.value.filter((ticket) => ticket.assigneeId === currentUserId)
  })

  const categoryOptions = computed(() => {
    const optionsMap = new Map()

    roleScopedTickets.value.forEach((ticket) => {
      if (ticket.categoryId || ticket.categoryName) {
        optionsMap.set(ticket.categoryId, {
          value: String(ticket.categoryId || ticket.categoryName),
          label: ticket.categoryName || `Категория #${ticket.categoryId}`,
        })
      }
    })

    return Array.from(optionsMap.values())
  })

  const filteredTickets = computed(() => {
    const normalizedQuery = filters.value.query.trim().toLowerCase()

    return roleScopedTickets.value.filter((ticket) => {
      const matchesStatus = !filters.value.status || ticket.status === filters.value.status
      const matchesPriority =
        isClient.value || !filters.value.priority || ticket.priority === filters.value.priority
      const ticketCategoryValue = String(ticket.categoryId || ticket.categoryName || '')
      const matchesCategory =
        !filters.value.category || ticketCategoryValue === filters.value.category

      const searchableText = [
        ticket.id,
        ticket.title,
        ticket.status,
        ...(isClient.value ? [] : [ticket.priority]),
        ticket.categoryName,
        ...(isClient.value ? [] : [ticket.assigneeName, ticket.assigneeId]),
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      const matchesQuery = !normalizedQuery || searchableText.includes(normalizedQuery)

      return matchesStatus && matchesPriority && matchesCategory && matchesQuery
    })
  })

  const paginatedTickets = computed(() => {
    const start = (page.value - 1) * pageSize.value
    return filteredTickets.value.slice(start, start + pageSize.value)
  })

  const total = computed(() => filteredTickets.value.length)

  const resetPage = () => {
    page.value = 1
  }

  const loadTickets = async () => {
    loading.value = true

    try {
      tickets.value = await getTicketsApi()
    } finally {
      loading.value = false
      resetPage()
    }
  }

  return {
    loading,
    filters,
    page,
    pageSize,
    isClient,
    categoryOptions,
    paginatedTickets,
    total,
    resetPage,
    loadTickets,
  }
}
