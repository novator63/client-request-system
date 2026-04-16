<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createTicketApi, getCategoriesForTicketApi } from '../api/tickets.api'
import { notifyApiError, notifySuccess, parseApiError } from '../utils/errorHandler'
import { emailLatinValidator, phoneValidator, normalizePhoneInput, formatPhoneWithPrefix, FIELD_LIMITS } from '../utils/validators'

const router = useRouter()

const loading = ref(false)
const categoriesLoading = ref(false)
const categories = ref([])
const categoriesError = ref('')

const formRef = ref(null)

const form = reactive({
  subject: '',
  description: '',
  customerName: '',
  customerEmail: '',
  customerPhone: '',
  category: '',
})

const hasCategories = computed(() => categories.value.length > 0)
const canSubmit = computed(() => !loading.value && !categoriesLoading.value && hasCategories.value)

const onPhoneInput = (value) => {
  form.customerPhone = normalizePhoneInput(value)
}

const formattedPhone = () => formatPhoneWithPrefix(form.customerPhone)

const rules = {
  subject: [
    { required: true, message: 'Введите тему заявки', trigger: 'blur' },
    {
      max: FIELD_LIMITS.subject,
      message: `Тема не должна превышать ${FIELD_LIMITS.subject} символов`,
      trigger: 'blur',
    },
  ],
  description: [
    { required: true, message: 'Введите описание', trigger: 'blur' },
    {
      max: FIELD_LIMITS.description,
      message: `Описание не должно превышать ${FIELD_LIMITS.description} символов`,
      trigger: 'blur',
    },
  ],
  customerName: [
    { required: true, message: 'Введите имя клиента', trigger: 'blur' },
    {
      max: FIELD_LIMITS.customerName,
      message: `Имя не должно превышать ${FIELD_LIMITS.customerName} символов`,
      trigger: 'blur',
    },
  ],
  customerEmail: [{ validator: emailLatinValidator, trigger: ['blur', 'change'] }],
  customerPhone: [{ validator: phoneValidator, trigger: ['blur', 'change'] }],
  category: [{ required: true, message: 'Выберите категорию', trigger: 'change' }],
}

const buildTicketDescription = () => {
  return [
    form.description.trim(),
    '',
    '--- Контактные данные ---',
    `Клиент: ${form.customerName.trim()}`,
    `Email: ${form.customerEmail.trim()}`,
    `Телефон: ${formattedPhone()}`,
  ]
    .join('\n')
    .trim()
}

const loadCategories = async () => {
  categoriesLoading.value = true
  categoriesError.value = ''
  categories.value = []

  try {
    const response = await getCategoriesForTicketApi()

    if (Array.isArray(response) && response.length > 0) {
      categories.value = response
      return
    }

    categoriesError.value = 'Список категорий пуст. Обратитесь к администратору.'
  } catch (error) {
    categoriesError.value = parseApiError(error, {
      fallbackMessage: 'Не удалось загрузить категории. Попробуйте обновить страницу.',
    })
  } finally {
    categoriesLoading.value = false
  }
}

const submit = async () => {
  if (!hasCategories.value) {
    if (categoriesLoading.value) {
      notifyApiError(null, {
        fallbackMessage: 'Пожалуйста, дождитесь загрузки категорий',
      })
      return
    }
    
    notifyApiError(null, {
      fallbackMessage: categoriesError.value || 'Список категорий недоступен. Обратитесь к администратору.',
    })
    return
  }

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  try {
    const payload = {
      title: form.subject.trim(),
      description: buildTicketDescription(),
      priority: 'MEDIUM',
      categoryId: Number(form.category),
    }

    const response = await createTicketApi(payload)

    notifySuccess('Заявка успешно создана')

    if (response?.id) {
      await router.push(`/tickets/${response.id}`)
      return
    }

    await router.push('/tickets')
  } catch (error) {
    notifyApiError(error, {
      fallbackMessage: 'Не удалось создать заявку. Попробуйте позже.',
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <section class="create-ticket-page">
    <el-card>
      <template #header>
        <h1 class="create-ticket-page__title">Создать заявку</h1>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submit"
      >
        <el-alert
          v-if="categoriesError"
          :title="categoriesError"
          type="error"
          show-icon
          class="create-ticket-page__error"
        />

        <el-form-item label="Тема" prop="subject">
          <el-input
            v-model="form.subject"
            :maxlength="FIELD_LIMITS.subject"
            show-word-limit
            placeholder="Кратко опишите проблему"
            :disabled="categoriesLoading"
          />
        </el-form-item>

        <el-form-item label="Описание" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            :maxlength="FIELD_LIMITS.description"
            show-word-limit
            placeholder="Подробно опишите ситуацию"
            :disabled="categoriesLoading"
          />
        </el-form-item>

        <el-form-item label="Имя клиента" prop="customerName">
          <el-input
            v-model="form.customerName"
            :maxlength="FIELD_LIMITS.customerName"
            show-word-limit
            placeholder="Например: Иван Иванов"
            :disabled="categoriesLoading"
          />
        </el-form-item>

        <el-form-item label="Email клиента" prop="customerEmail">
          <el-input
            v-model="form.customerEmail"
            :maxlength="FIELD_LIMITS.customerEmail"
            show-word-limit
            placeholder="Например: client@example.com"
            :disabled="categoriesLoading"
          />
        </el-form-item>

        <el-form-item label="Телефон клиента" prop="customerPhone">
          <el-input
            :model-value="form.customerPhone"
            :maxlength="FIELD_LIMITS.customerPhone"
            placeholder="900-200-30-40"
            @input="onPhoneInput"
            :disabled="categoriesLoading"
          >
            <template #prepend>+7</template>
          </el-input>
        </el-form-item>

        <el-form-item label="Категория" prop="category">
          <el-select
            v-model="form.category"
            :placeholder="categoriesLoading ? 'Загрузка категорий...' : (hasCategories ? 'Выберите категорию' : 'Категории недоступны')"
            :loading="categoriesLoading"
            :disabled="!hasCategories"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="String(category.id)"
            />
          </el-select>
        </el-form-item>

        <div class="create-ticket-page__actions">
          <el-button @click="router.push('/tickets')" :disabled="loading || categoriesLoading">Отмена</el-button>
          <el-button type="primary" :loading="loading" :disabled="!canSubmit" @click="submit">
            Создать заявку
          </el-button>
        </div>
      </el-form>
    </el-card>
  </section>
</template>

<style scoped>
.create-ticket-page {
  min-height: 100%;
}

.create-ticket-page__title {
  margin: 0;
  font-size: 24px;
}

.create-ticket-page :deep(.el-select) {
  width: 100%;
}

.create-ticket-page__error {
  margin-bottom: 16px;
}

.create-ticket-page__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
