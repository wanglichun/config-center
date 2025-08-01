import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      noAuth: true
    }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layout/Layout.vue'),
    meta: {
      title: '首页'
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '仪表盘',
          icon: 'Dashboard'
        }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/Index.vue'),
        meta: {
          title: '配置管理',
          icon: 'Setting'
        }
      },
      {
        path: 'config/detail/:id',
        name: 'ConfigDetail',
        component: () => import('@/views/config/Detail.vue'),
        meta: {
          title: '配置详情',
          hidden: true
        }
      },
      {
        path: 'config/publish/:id',
        name: 'ConfigPublishDetail',
        component: () => import('@/views/config/PublishDetail.vue'),
        meta: {
          title: '配置发布详情',
          hidden: true
        }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: {
          title: '变更历史',
          icon: 'Clock'
        }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          role: ['ADMIN']
        }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/Monitor.vue'),
        meta: {
          title: '系统监控',
          icon: 'Monitor'
        }
      },
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('@/views/ticket/Index.vue'),
        meta: {
          title: '工单管理',
          icon: 'Tickets'
        }
      },
      {
        path: 'ticket/detail/:id',
        name: 'TicketDetail',
        component: () => import('@/views/ticket/Detail.vue'),
        meta: {
          title: '工单详情',
          hidden: true
        }
      },
      {
        path: 'config/edit/:id',
        name: 'EditConfig',
        component: () => import('@/views/config/Edit.vue'),
        meta: {
          title: '编辑配置',
          hidden: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: {
          title: '个人中心',
          hidden: true
        }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: {
      title: '页面不存在',
      noAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  
  // 设置页面标题 - 支持国际化
  const baseTitle = '配置中心'
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${baseTitle}`
    // 设置favicon（如果需要动态改变的话）
    const link: HTMLLinkElement = document.querySelector("link[rel*='icon']") || document.createElement('link')
    link.type = 'image/svg+xml'
    link.rel = 'icon'
    link.href = '/favicon.svg'
    document.getElementsByTagName('head')[0].appendChild(link)
  } else {
    document.title = baseTitle
  }
  
  // 不需要认证的页面
  if (to.meta.noAuth) {
    next()
    return
  }
  
  // 检查是否已登录
  if (!userStore.token) {
    next('/login')
    return
  }
  
  // 检查用户信息（登录时已经获取了用户信息，这里只需要验证是否存在）
  if (!userStore.userInfo) {
    // 如果没有用户信息，说明token无效，直接跳转到登录页
    userStore.logout()
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.role && to.meta.role.length > 0) {
    if (!to.meta.role.includes(userStore.userInfo?.role)) {
      ElMessage.error('权限不足')
      next('/dashboard')
      return
    }
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router 