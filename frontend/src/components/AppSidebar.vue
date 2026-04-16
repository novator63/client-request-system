<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  { label: 'Заявки', path: '/' },
  { label: 'Создать заявку', path: '/requests/create' },
  { label: 'Категории', path: '/categories' },
  { label: 'Отчеты', path: '/reports' },
]
</script>

<template>
  <aside class="app-sidebar">
    <el-menu :default-active="route.path" router class="app-sidebar__menu">
      <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
        {{ item.label }}
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<style scoped>
.app-sidebar {
  height: 100%;
  background: #0f172a;
  border-right: 1px solid #1e293b;
}

.app-sidebar__menu {
  min-height: 100%;
  border-right: 0;
  background: transparent;
}

.app-sidebar :deep(.el-menu-item) {
  color: #d1d5db;
  font-weight: 500;
}

.app-sidebar :deep(.el-menu-item:hover) {
  background-color: #1e293b;
  color: #ffffff;
}

.app-sidebar :deep(.el-menu-item.is-active) {
  background-color: #1d4ed8;
  color: #ffffff;
}
</style>
