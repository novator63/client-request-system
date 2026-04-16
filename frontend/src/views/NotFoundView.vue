<template>
  <main class="not-found-page">
    <el-card class="not-found-card">
      <h1>404</h1>
      <p>Страница не найдена.</p>
      <router-link to="/">
        <el-button type="primary">На главную</el-button>
      </router-link>
    </el-card>
  </main>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
  background: #f8fafc;
}

.not-found-card {
  width: 100%;
  max-width: 420px;
  text-align: center;
}

h1 {
  margin: 0 0 8px;
  font-size: 42px;
}

p {
  margin: 0 0 16px;
  color: #4b5563;
}
</style>
