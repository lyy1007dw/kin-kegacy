import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', layout: 'blank' }
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页概览', icon: 'Home' }
      },
      {
        path: '/family',
        name: 'Family',
        component: () => import('@/views/Family.vue'),
        meta: { title: '家谱管理', icon: 'People' }
      },
      {
        path: '/family/:id',
        name: 'FamilyDetail',
        component: () => import('@/views/FamilyDetail.vue'),
        meta: { title: '家谱详情', hidden: true }
      },
      {
        path: '/member',
        name: 'Member',
        component: () => import('@/views/Member.vue'),
        meta: { title: '成员管理', icon: 'Person' }
      },
      {
        path: '/approval',
        name: 'Approval',
        component: () => import('@/views/Approval.vue'),
        meta: { title: '审批管理', icon: 'Clipboard' }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '用户管理', icon: 'Settings' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  document.title = `${to.meta.title || '家谱传承'} - 家谱管理系统`
  
  if (to.meta.layout === 'blank') {
    next()
    return
  }

  if (!userStore.token && to.path !== '/login') {
    next('/login')
    return
  }

  if (userStore.token && to.path === '/login') {
    next('/')
    return
  }

  if (to.path !== '/login' && to.path !== '/') {
    if (!userStore.userInfo || userStore.userInfo.globalRole !== 'SUPER_ADMIN') {
      next('/login')
      userStore.logout()
      return
    }
  }

  next()
})

export default router
