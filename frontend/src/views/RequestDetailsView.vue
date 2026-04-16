<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTicketByIdApi } from '../api/tickets.api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const ticket = ref(null)
const notFound = ref(false)

const ticketId = computed(() => route.params.id)
const isClient = computed(() => authStore.user?.role === 'CLIENT')

const loadTicket = async () => {
  loading.value = true
  notFound.value = false

  try {
    ticket.value = await getTicketByIdApi(ticketId.value)
  } catch (error) {
    if (error.response?.status === 404) {
      notFound.value = true
      return
    }

    throw error
  } finally {
    loading.value = false
  }
}

const formatDate = (value) => {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('ru-RU')
}

onMounted(loadTicket)
</script>

<template>
  <section>
    <el-card v-loading="loading">
      <template #header>
        <div class="details-header">
          <h1>Заявка #{{ ticketId }}</h1>
          <el-button @click="router.push('/tickets')">К списку</el-button>
        </div>
      </template>

      <el-empty v-if="notFound" description="Заявка не найдена" />

      <el-descriptions v-else-if="ticket" :column="1" border>
        <el-descriptions-item label="Тема">{{ ticket.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Описание">{{
          ticket.description || '—'
        }}</el-descriptions-item>
        <el-descriptions-item label="Статус">{{ ticket.status || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isClient" label="Приоритет">{{
          ticket.priority || '—'
        }}</el-descriptions-item>
        <el-descriptions-item label="Категория">{{
          ticket.categoryName || '—'
        }}</el-descriptions-item>
        <el-descriptions-item label="Автор">{{
          ticket.authorName || `ID: ${ticket.authorId || '—'}`
        }}</el-descriptions-item>
        <el-descriptions-item v-if="!isClient" label="Ответственный">
          {{
            ticket.assigneeName || (ticket.assigneeId ? `ID: ${ticket.assigneeId}` : 'Не назначен')
          }}
        </el-descriptions-item>
        <el-descriptions-item label="Срок">{{ formatDate(ticket.dueAt) }}</el-descriptions-item>
        <el-descriptions-item label="Создана">{{
          formatDate(ticket.createdAt)
        }}</el-descriptions-item>
        <el-descriptions-item label="Обновлена">{{
          formatDate(ticket.updatedAt)
        }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </section>
</template>

<style scoped>
.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.details-header h1 {
  margin: 0;
  font-size: 24px;
}
</style>
