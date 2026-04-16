import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppLayout from '../layouts/AppLayout.vue'
import CategoriesView from '../views/CategoriesView.vue'
import CreateTicketView from '../views/CreateTicketView.vue'
import LoginView from '../views/LoginView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import TicketDetailsView from '../views/TicketDetailsView.vue'
import ReportsView from '../views/ReportsView.vue'
import TicketsView from '../views/TicketsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/tickets',
        },
        {
          path: 'tickets',
          name: 'tickets',
          component: TicketsView,
        },
        {
          path: 'tickets/create',
          name: 'create-ticket',
          component: CreateTicketView,
        },
        {
          path: 'tickets/:id',
          name: 'ticket-details',
          component: TicketDetailsView,
        },
        {
          path: 'categories',
          name: 'categories',
          component: CategoriesView,
          meta: { allowedRoles: ['ADMIN', 'OPERATOR'] },
        },
        {
          path: 'reports',
          name: 'reports',
          component: ReportsView,
          meta: { allowedRoles: ['ADMIN'] },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  await authStore.initialize()

  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const guestOnly = to.matched.some((record) => record.meta.guestOnly)
  const allowedRoles = to.matched
    .map((record) => record.meta.allowedRoles)
    .find((roles) => Array.isArray(roles) && roles.length > 0)

  if (requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (guestOnly && authStore.isAuthenticated) {
    return { name: 'tickets' }
  }

  if (allowedRoles?.length) {
    const currentRole = authStore.user?.role

    if (!currentRole || !allowedRoles.includes(currentRole)) {
      return { name: 'tickets' }
    }
  }

  return true
})

export default router
