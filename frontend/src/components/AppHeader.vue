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
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 24px;
  background: linear-gradient(110deg, #f6f9ff 10%, #eef3ff 100%);
  border-bottom: 1px solid #dce5f5;
}

.app-header__title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: -0.5px;
}

.app-header__user-block {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-header__user-info {
  text-align: right;
  min-width: 140px;
}

.app-header__name {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.app-header__role {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

@media (max-width: 768px) {
  .app-header {
    min-height: auto;
    padding: 14px 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
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
