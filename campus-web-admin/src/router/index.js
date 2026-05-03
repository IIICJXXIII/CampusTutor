import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'tutors',
        name: 'Tutors',
        component: () => import('@/views/tutor/TutorList.vue'),
        meta: { title: '教师管理', icon: 'UserFilled' }
      },
      {
        path: 'tutor-audit',
        name: 'TutorAudit',
        component: () => import('@/views/tutor/TutorAudit.vue'),
        meta: { title: '认证审核', icon: 'Stamp' }
      },
      {
        path: 'parents',
        name: 'Parents',
        component: () => import('@/views/parent/ParentList.vue'),
        meta: { title: '家长管理', icon: 'Avatar' }
      },
      {
        path: 'demands',
        name: 'Demands',
        component: () => import('@/views/demand/DemandList.vue'),
        meta: { title: '需求管理', icon: 'Document' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单管理', icon: 'List' }
      },
      {
        path: 'lessons',
        name: 'Lessons',
        component: () => import('@/views/teaching/LessonList.vue'),
        meta: { title: '课时记录', icon: 'Calendar' }
      },
      {
        path: 'matches',
        name: 'Matches',
        component: () => import('@/views/match/MatchList.vue'),
        meta: { title: '匹配记录', icon: 'Connection' }
      },
      {
        path: 'wallets',
        name: 'Wallets',
        component: () => import('@/views/wallet/WalletList.vue'),
        meta: { title: '钱包管理', icon: 'Wallet' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/CommunityPostList.vue'),
        meta: { title: '社区管理', icon: 'ChatDotRound' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/system/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '管理后台'} - 素质教育平台`
  
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
