<script setup>
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { user } = storeToRefs(authStore)

const logout = async () => {
	authStore.logout()
	await router.push('/login')
}
</script>

<template>
	<main class="home-page">
		<el-card class="home-card">
			<h1>Вы успешно вошли</h1>
			<p v-if="user">Пользователь: {{ user.fullName }} ({{ user.email }})</p>
			<p v-else>Пользователь загружается...</p>

			<el-button type="danger" @click="logout">Выйти</el-button>
		</el-card>
	</main>
</template>

<style scoped>
.home-page {
	min-height: 100vh;
	display: grid;
	place-items: center;
	padding: 16px;
	background: linear-gradient(135deg, #f9fff7, #eef8ef);
}

.home-card {
	width: 100%;
	max-width: 640px;
}
</style>
