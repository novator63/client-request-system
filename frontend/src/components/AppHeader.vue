<script setup>
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const { user } = storeToRefs(authStore)

defineEmits(['logout'])
</script>

<template>
  <header class="app-header">
    <div class="app-header__title">Client Request System</div>

    <div class="app-header__user-block">
      <div class="app-header__user-info">
        <div class="app-header__name">{{ user?.fullName || 'Пользователь' }}</div>
        <div class="app-header__role">Роль: {{ user?.role || 'N/A' }}</div>
      </div>

      <el-button type="danger" plain @click="$emit('logout')">Выйти</el-button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 20px;
  background: linear-gradient(110deg, #f6f9ff 10%, #eef3ff 100%);
  border-bottom: 1px solid #dce5f5;
}

.app-header__title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.app-header__user-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-header__user-info {
  text-align: right;
}

.app-header__name {
  font-weight: 600;
  color: #111827;
}

.app-header__role {
  font-size: 13px;
  color: #4b5563;
}

@media (max-width: 768px) {
  .app-header {
    height: auto;
    padding: 12px;
    flex-direction: column;
    align-items: flex-start;
  }

  .app-header__user-block {
    width: 100%;
    justify-content: space-between;
  }

  .app-header__user-info {
    text-align: left;
  }
}
</style>
