<script setup>
import { computed, onMounted, ref } from 'vue'
import { getCategoriesForTicketApi } from '../api/tickets.api'
import { parseApiError } from '../utils/errorHandler'

const loading = ref(false)
const categories = ref([])
const loadError = ref('')

const total = computed(() => categories.value.length)

const loadCategories = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const response = await getCategoriesForTicketApi()
    categories.value = Array.isArray(response) ? response : []
  } catch (error) {
    categories.value = []
    loadError.value = parseApiError(error, {
      fallbackMessage: 'Не удалось загрузить категории. Попробуйте обновить страницу.',
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <section class="categories-page">
    <el-card>
      <template #header>
        <div class="categories-page__header">
          <h1>Категории заявок</h1>
          <el-button :loading="loading" @click="loadCategories">Обновить</el-button>
        </div>
      </template>

      <el-alert v-if="loadError" :title="loadError" type="error" show-icon class="categories-page__alert" />

      <el-empty
        v-else-if="!loading && total === 0"
        description="Категории пока не добавлены"
      />

      <el-table v-else :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="Название" min-width="240" />
      </el-table>
    </el-card>
  </section>
</template>

<style scoped>
.categories-page {
  min-height: 100%;
}

.categories-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.categories-page__header h1 {
  margin: 0;
  font-size: 24px;
}

.categories-page__alert {
  margin-bottom: 12px;
}
</style>
