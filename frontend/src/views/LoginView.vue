<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { parseApiError } from '../utils/errorHandler'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: '',
})

const rules = {
  email: [
    { required: true, message: 'Введите email', trigger: 'blur' },
    { type: 'email', message: 'Некорректный email', trigger: ['blur', 'change'] },
  ],
  password: [{ required: true, message: 'Введите пароль', trigger: 'blur' }],
}

const formRef = ref(null)

const submit = async () => {
  errorMessage.value = ''

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  try {
    await authStore.login({
      email: form.email,
      password: form.password,
    })

    const redirect = route.query.redirect || '/'
    await router.push(redirect)
  } catch (error) {
    errorMessage.value = parseApiError(error, {
      fallbackMessage: 'Не удалось войти. Проверьте email и пароль.',
    })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <el-card class="login-card">
      <template #header>
        <h1 class="title">Вход в систему</h1>
      </template>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        class="error-alert"
      />

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="submit"
      >
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Введите email" />
        </el-form-item>

        <el-form-item label="Пароль" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="Введите пароль"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">
          Войти
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
  background: linear-gradient(120deg, #f3f7ff, #e9f0ff);
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.title {
  margin: 0;
  font-size: 24px;
}

.error-alert {
  margin-bottom: 16px;
}

.submit-btn {
  width: 100%;
}
</style>
