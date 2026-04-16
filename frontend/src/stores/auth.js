import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi } from '../api/auth.api'

const TOKEN_STORAGE_KEY = 'auth_token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_STORAGE_KEY),
    user: null,
    initialized: false,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem(TOKEN_STORAGE_KEY, token)
    },

    clearAuth() {
      this.token = null
      this.user = null
      localStorage.removeItem(TOKEN_STORAGE_KEY)
    },

    async initialize() {
      if (this.initialized) {
        return
      }

      if (this.token) {
        try {
          await this.fetchCurrentUser()
        } catch {
          this.clearAuth()
        }
      }

      this.initialized = true
    },

    async login(credentials) {
      const response = await loginApi(credentials)

      if (!response.accessToken) {
        throw new Error('Access token was not returned by API')
      }

      this.setToken(response.accessToken)
      await this.fetchCurrentUser()
    },

    async fetchCurrentUser() {
      if (!this.token) {
        this.user = null
        return null
      }

      const user = await getCurrentUserApi()
      this.user = user
      return user
    },

    logout() {
      this.clearAuth()
      this.initialized = true
    },
  },
})