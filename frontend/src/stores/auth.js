import { defineStore } from 'pinia'
import { getCurrentUserApi, loginApi, refreshTokenApi, logoutApi } from '../api/auth.api'
import { setAuthToken, clearAuthToken } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    user: null,
    initialized: false,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },

  actions: {
    /**
     * Set token and update axios interceptor
     */
    setToken(token) {
      this.token = token
      setAuthToken(token)
    },

    /**
     * Clear auth state completely
     */
    clearAuth() {
      this.token = null
      this.user = null
      clearAuthToken()
    },

    /**
     * Initialize app on startup - try to restore session via refresh
     */
    async initialize() {
      if (this.initialized) {
        return
      }

      try {
        // Try to refresh/restore session
        await this.refresh()
        // If refresh succeeded, fetch current user
        await this.fetchCurrentUser()
      } catch {
        // Refresh failed - user is not authenticated
        this.clearAuth()
      }

      this.initialized = true
    },

    /**
     * Refresh access token using httpOnly refresh cookie
     */
    async refresh() {
      try {
        const response = await refreshTokenApi()

        if (!response.accessToken) {
          throw new Error('Access token was not returned by refresh endpoint')
        }

        this.setToken(response.accessToken)
        return response
      } catch (error) {
        this.clearAuth()
        throw error
      }
    },

    /**
     * Login with email and password
     */
    async login(credentials) {
      const response = await loginApi(credentials)

      if (!response.accessToken) {
        throw new Error('Access token was not returned by API')
      }

      this.setToken(response.accessToken)
      await this.fetchCurrentUser()
    },

    /**
     * Fetch and store current user info
     */
    async fetchCurrentUser() {
      if (!this.token) {
        this.user = null
        return null
      }

      const user = await getCurrentUserApi()
      this.user = user
      return user
    },

    /**
     * Logout - clear session on backend and frontend
     */
    async logout() {
      try {
        // Call backend logout to clear refresh cookie
        await logoutApi()
      } catch {
        // Even if backend logout fails, we still clear frontend state
        // to ensure user is logged out locally
      } finally {
        this.clearAuth()
        this.initialized = true
      }
    },
  },
})
