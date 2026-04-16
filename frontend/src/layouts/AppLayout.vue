<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppHeader from '../components/AppHeader.vue'
import AppSidebar from '../components/AppSidebar.vue'

const router = useRouter()
const authStore = useAuthStore()
const isLoggingOut = ref(false)

const handleLogout = async () => {
  isLoggingOut.value = true
  try {
    await authStore.logout()
    await router.push('/login')
  } finally {
    isLoggingOut.value = false
  }
}
</script>

<template>
  <div class="app-shell">
    <AppHeader @logout="handleLogout" />

    <div class="app-shell__body">
      <div class="app-shell__sidebar">
        <AppSidebar />
      </div>

      <main class="app-shell__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.app-shell__body {
  flex: 1;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  min-height: 0;
}

.app-shell__sidebar {
  min-height: calc(100vh - 72px);
}

.app-shell__content {
  padding: 20px;
}

@media (max-width: 992px) {
  .app-shell__body {
    grid-template-columns: 1fr;
  }

  .app-shell__sidebar {
    min-height: auto;
  }
}
</style>
