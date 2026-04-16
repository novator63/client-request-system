<script setup>
import { computed, onMounted, ref } from 'vue'
import { getTicketsApi } from '../api/tickets.api'
import { isClosedTicketStatus, STATUS_OPTIONS } from '../constants/ticket.constants'
import { getStatusLabel } from '../utils/ticketFormatters'
import { parseApiError } from '../utils/errorHandler'

const loading = ref(false)
const tickets = ref([])
const loadError = ref('')

const totalTickets = computed(() => tickets.value.length)

const overdueTickets = computed(() => {
  const now = Date.now()
  return tickets.value.filter((ticket) => {
    if (!ticket?.dueAt || isClosedTicketStatus(ticket.status)) {
      return false
    }

    const dueAt = new Date(ticket.dueAt).getTime()
    return Number.isFinite(dueAt) && dueAt < now
  }).length
})

const statusRows = computed(() => {
  const counter = new Map(STATUS_OPTIONS.map((item) => [item.value, 0]))

  tickets.value.forEach((ticket) => {
    if (!counter.has(ticket.status)) {
      counter.set(ticket.status, 0)
    }

    counter.set(ticket.status, counter.get(ticket.status) + 1)
  })

  return Array.from(counter.entries()).map(([status, count]) => ({
    status,
    label: getStatusLabel(status),
    count,
  }))
})

const loadReport = async () => {
  loading.value = true
  loadError.value = ''

  try {
    tickets.value = await getTicketsApi()
  } catch (error) {
    tickets.value = []
    loadError.value = parseApiError(error, {
      fallbackMessage: 'Не удалось загрузить данные для отчета. Попробуйте обновить страницу.',
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadReport)
</script>

<template>
  <section class="reports-page">
    <el-card>
      <template #header>
        <div class="reports-page__header">
          <h1>Отчеты по заявкам</h1>
          <el-button :loading="loading" @click="loadReport">Обновить</el-button>
        </div>
      </template>

      <el-alert v-if="loadError" :title="loadError" type="error" show-icon class="reports-page__alert" />

      <div class="reports-page__stats" v-loading="loading">
        <el-statistic title="Всего заявок" :value="totalTickets" />
        <el-statistic title="Просроченные" :value="overdueTickets" />
      </div>

      <el-table :data="statusRows" stripe>
        <el-table-column prop="label" label="Статус" min-width="220" />
        <el-table-column prop="count" label="Количество" width="160" />
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.reports-page {
  min-height: 100%;
}

.reports-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.reports-page__header h1 {
  margin: 0;
  font-size: 24px;
}

.reports-page__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.reports-page__alert {
  margin-bottom: 12px;
}

@media (max-width: 700px) {
  .reports-page__stats {
    grid-template-columns: 1fr;
  }
}
</style>
