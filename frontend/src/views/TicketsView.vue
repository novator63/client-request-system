<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { getTicketsApi } from '../api/tickets.api'

const router = useRouter()
const authStore = useAuthStore()

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

const isClient = computed(() => authStore.user?.role === 'CLIENT')
const isOperator = computed(() => authStore.user?.role === 'OPERATOR')

const statusOptions = [
  { label: 'Новый', value: 'NEW' },
  { label: 'В работе', value: 'IN_PROGRESS' },
  { label: 'Решен', value: 'RESOLVED' },
  { label: 'Закрыт', value: 'CLOSED' },
]

const priorityOptions = [
  { label: 'Низкий', value: 'LOW' },
  { label: 'Средний', value: 'MEDIUM' },
  { label: 'Высокий', value: 'HIGH' },
]

const categoryOptions = computed(() => {
  const map = new Map()

  roleScopedTickets.value.forEach((ticket) => {
    if (ticket.categoryId || ticket.categoryName) {
      map.set(ticket.categoryId, {
        value: String(ticket.categoryId || ticket.categoryName),
        label: ticket.categoryName || `Категория #${ticket.categoryId}`,
      })
    }
  })

  return Array.from(map.values())
})

const roleScopedTickets = computed(() => {
  if (!isOperator.value) {
    return tickets.value
  }

  const currentUserId = authStore.user?.id

  return tickets.value.filter((ticket) => ticket.assigneeId === currentUserId)
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

const statusLabel = (value) =>
  statusOptions.find((option) => option.value === value)?.label || value || '—'
const priorityLabel = (value) =>
  priorityOptions.find((option) => option.value === value)?.label || value || '—'

const dueAtLabel = (value) => {
  if (!value) {
    return '—'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('ru-RU')
}

const assigneeLabel = (ticket) => {
  if (ticket.assigneeName) {
    return ticket.assigneeName
  }

  if (ticket.assigneeId) {
    return `ID: ${ticket.assigneeId}`
  }

  return 'Не назначен'
}

const openTicket = (ticket) => {
  if (!ticket?.id) {
    return
  }

  router.push(`/tickets/${ticket.id}`)
}

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

onMounted(loadTickets)
</script>

<template>
  <section class="tickets-page">
    <el-card>
      <template #header>
        <div class="tickets-page__header">
          <h1>Заявки</h1>
          <el-button :loading="loading" @click="loadTickets">Обновить</el-button>
        </div>
      </template>

      <div class="tickets-page__filters">
        <el-select v-model="filters.status" placeholder="Статус" clearable @change="resetPage">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select
          v-if="!isClient"
          v-model="filters.priority"
          placeholder="Приоритет"
          clearable
          @change="resetPage"
        >
          <el-option
            v-for="option in priorityOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select v-model="filters.category" placeholder="Категория" clearable @change="resetPage">
          <el-option
            v-for="option in categoryOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-input
          v-model="filters.query"
          placeholder="Поиск по номеру, теме, категории..."
          clearable
          @input="resetPage"
        />
      </div>

      <el-table :data="paginatedTickets" v-loading="loading" stripe @row-click="openTicket">
        <el-table-column prop="id" label="Номер" min-width="90" />
        <el-table-column prop="title" label="Тема" min-width="220" show-overflow-tooltip />
        <el-table-column label="Статус" min-width="130">
          <template #default="scope">{{ statusLabel(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column v-if="!isClient" label="Приоритет" min-width="130">
          <template #default="scope">{{ priorityLabel(scope.row.priority) }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="Категория" min-width="160">
          <template #default="scope">{{ scope.row.categoryName || '—' }}</template>
        </el-table-column>
        <el-table-column v-if="!isClient" label="Ответственный" min-width="160">
          <template #default="scope">{{ assigneeLabel(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="Срок" min-width="170">
          <template #default="scope">{{ dueAtLabel(scope.row.dueAt) }}</template>
        </el-table-column>
        <el-table-column label="" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click.stop="openTicket(scope.row)">Открыть</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="tickets-page__pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.tickets-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tickets-page__header h1 {
  margin: 0;
  font-size: 24px;
}

.tickets-page__filters {
  margin-bottom: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.tickets-page__pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .tickets-page__filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .tickets-page__filters {
    grid-template-columns: 1fr;
  }

  .tickets-page__pagination {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
