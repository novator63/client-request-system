<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTicketsList } from '../composables/useTicketsList'
import { STATUS_OPTIONS } from '../constants/ticket.constants'
import { formatDateTime, getAssigneeLabel, getPriorityLabel, getStatusLabel } from '../utils/ticketFormatters'

const router = useRouter()
const authStore = useAuthStore()
const {
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
} = useTicketsList({ authStore })

const openTicket = (ticket) => {
  if (!ticket?.id) {
    return
  }

  router.push(`/tickets/${ticket.id}`)
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
            v-for="option in STATUS_OPTIONS"
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
            v-for="option in PRIORITY_OPTIONS"
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
          <template #default="scope">{{ getStatusLabel(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column v-if="!isClient" label="Приоритет" min-width="130">
          <template #default="scope">{{ getPriorityLabel(scope.row.priority) }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="Категория" min-width="160">
          <template #default="scope">{{ scope.row.categoryName || '—' }}</template>
        </el-table-column>
        <el-table-column v-if="!isClient" label="Ответственный" min-width="160">
          <template #default="scope">{{ getAssigneeLabel(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="Срок" min-width="170">
          <template #default="scope">{{ formatDateTime(scope.row.dueAt) }}</template>
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
