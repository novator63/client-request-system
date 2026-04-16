<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getReportByCategoryApi,
  getReportByStatusApi,
  getReportSummaryApi,
} from '../api/reports.api'
import { getStatusLabel, normalizeSummary, normalizeRows } from '../utils/ticketFormatters'
import { parseApiError } from '../utils/errorHandler'

const loading = ref(false)
const summary = ref({
  total: 0,
  open: 0,
  inProgress: 0,
  closed: 0,
  overdue: 0,
})
const statusRows = ref([])
const categoryRows = ref([])
const loadError = ref('')

const summaryCards = computed(() => [
  { key: 'total', title: 'Всего', value: summary.value.total },
  { key: 'inProgress', title: 'В работе', value: summary.value.inProgress },
  { key: 'closed', title: 'Закрыто', value: summary.value.closed },
  { key: 'overdue', title: 'Просрочено', value: summary.value.overdue },
])

const loadReport = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const [summaryResponse, statusResponse, categoryResponse] = await Promise.all([
      getReportSummaryApi(),
      getReportByStatusApi(),
      getReportByCategoryApi(),
    ])

    summary.value = normalizeSummary(summaryResponse)
    statusRows.value = normalizeRows(statusResponse)
    categoryRows.value = normalizeRows(categoryResponse)
  } catch (error) {
    summary.value = {
      total: 0,
      open: 0,
      inProgress: 0,
      closed: 0,
      overdue: 0,
    }
    statusRows.value = []
    categoryRows.value = []
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
    <el-card class="reports-page__card" v-loading="loading">
      <template #header>
        <div class="reports-page__header">
          <div>
            <h1>Отчеты по заявкам</h1>
            <p>Агрегированные данные по заявкам, статусам и категориям</p>
          </div>
          <el-button :loading="loading" @click="loadReport">Обновить</el-button>
        </div>
      </template>

      <el-alert v-if="loadError" :title="loadError" type="error" show-icon class="reports-page__alert" />

      <template v-if="!loadError">
        <div class="reports-page__stats">
          <el-card v-for="card in summaryCards" :key="card.key" shadow="never" class="reports-page__stat">
            <div class="reports-page__stat-title">{{ card.title }}</div>
            <div class="reports-page__stat-value">{{ card.value }}</div>
          </el-card>
        </div>

        <div class="reports-page__sections">
          <el-card shadow="never" class="reports-page__section">
            <template #header>
              <div class="reports-page__section-header">
                <h2>По статусам</h2>
              </div>
            </template>

            <el-table v-if="statusRows.length" :data="statusRows" stripe>
              <el-table-column label="Статус" min-width="220">
                <template #default="scope">{{ getStatusLabel(scope.row.name) }}</template>
              </el-table-column>
              <el-table-column prop="count" label="Количество" width="160" align="right" />
            </el-table>

            <el-empty v-else description="Нет данных по статусам" />
          </el-card>

          <el-card shadow="never" class="reports-page__section">
            <template #header>
              <div class="reports-page__section-header">
                <h2>По категориям</h2>
              </div>
            </template>

            <el-table v-if="categoryRows.length" :data="categoryRows" stripe>
              <el-table-column prop="name" label="Категория" min-width="220" show-overflow-tooltip />
              <el-table-column prop="count" label="Количество" width="160" align="right" />
            </el-table>

            <el-empty v-else description="Нет данных по категориям" />
          </el-card>
        </div>
      </template>

      <el-empty v-else description="Не удалось загрузить данные отчета" />
    </el-card>
  </section>
</template>

<style scoped>
.reports-page {
  min-height: 100%;
}

.reports-page__card {
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

.reports-page__header p {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.reports-page__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.reports-page__stat {
  border-radius: 12px;
}

.reports-page__stat-title {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}

.reports-page__stat-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.reports-page__alert {
  margin-bottom: 12px;
}

.reports-page__sections {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.reports-page__section {
  min-width: 0;
}

.reports-page__section-header h2 {
  margin: 0;
  font-size: 18px;
}

@media (max-width: 700px) {
  .reports-page__stats {
    grid-template-columns: 1fr;
  }

  .reports-page__sections {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1200px) {
  .reports-page__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
