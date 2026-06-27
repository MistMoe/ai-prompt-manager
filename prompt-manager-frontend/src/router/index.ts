import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/',
      component: () => import('../layouts/AppLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'prompts',
          name: 'Prompts',
          component: () => import('../views/PromptListView.vue')
        },
        {
          path: 'favorites',
          name: 'Favorites',
          component: () => import('../views/FavoritesView.vue')
        },
        {
          path: 'template-preview',
          name: 'TemplatePreview',
          component: () => import('../views/TemplatePreviewView.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const publicPages = ['/login', '/register']
  const token = localStorage.getItem('token')

  if (!publicPages.includes(to.path) && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router