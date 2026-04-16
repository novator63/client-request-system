import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  withCredentials: true,
})

// In-memory token storage
let authToken = null
let isRefreshing = false
let refreshSubscribers = []

const isAuthFlowEndpoint = (url = '') => {
  return url.includes('/auth/login') || url.includes('/auth/refresh')
}

/**
 * Set the in-memory auth token
 */
export const setAuthToken = (token) => {
  authToken = token
}

/**
 * Clear the in-memory auth token
 */
export const clearAuthToken = () => {
  authToken = null
}

/**
 * Subscribe to refresh completion
 */
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback)
}

/**
 * Notify all subscribers that refresh is completed
 */
const notifyTokenRefreshed = (token) => {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

// Request interceptor - add Authorization header
http.interceptors.request.use((config) => {
  // Do not attach token only for login/refresh endpoints.
  if (isAuthFlowEndpoint(config.url)) {
    return config
  }

  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`
  }

  return config
})

// Response interceptor - handle 401 with refresh flow
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Only handle 401 for non-auth endpoints and not already retried
    if (
      error.response?.status === 401 &&
      !isAuthFlowEndpoint(originalRequest?.url) &&
      !originalRequest._isRetry
    ) {
      // Mark request as retry to prevent infinite loop
      originalRequest._isRetry = true

      try {
        // If refresh is already in progress, wait for it
        if (isRefreshing) {
          return new Promise((resolve) => {
            subscribeTokenRefresh((token) => {
              // Retry original request with new token
              originalRequest.headers.Authorization = `Bearer ${token}`
              resolve(http(originalRequest))
            })
          })
        }

        // Start refresh process
        isRefreshing = true

        const authStore = useAuthStore()
        await authStore.refresh()

        // Get the new token from the store
        if (authStore.token) {
          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${authStore.token}`
          notifyTokenRefreshed(authStore.token)
          return http(originalRequest)
        }

        // If refresh failed, clear auth and redirect
        await authStore.clearAuth()
        window.location.assign('/login')
        return Promise.reject(error)
      } catch {
        // Refresh failed - clear auth and redirect
        const authStore = useAuthStore()
        await authStore.clearAuth()
        notifyTokenRefreshed(null)
        window.location.assign('/login')
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default http
