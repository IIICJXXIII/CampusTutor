/**
 * 教师端路由配置
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // 默认跳转
  {
    path: '/',
    redirect: '/login'
  },

  // === 公共页面 (无需登录) ===
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },

  // === 主布局 ===
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // 首页 - 找学生
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/FindStudents.vue'),
        meta: { title: '找学生' }
      },
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('@/views/home/StudentList.vue'),
        meta: { title: '学生列表' }
      },
      {
        path: 'demand/:id',
        name: 'DemandDetail',
        component: () => import('@/views/home/DemandDetail.vue'),
        meta: { title: '需求详情' }
      },

      // 资质认证
      {
        path: 'auth',
        name: 'TeacherAuth',
        component: () => import('@/views/certification/TeacherAuth.vue'),
        meta: { title: '资质认证' }
      },

      // 我的简历
      {
        path: 'resume',
        name: 'MyResume',
        component: () => import('@/views/resume/MyResume.vue'),
        meta: { title: '我的简历' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/resume/Schedule.vue'),
        meta: { title: '排课设置' }
      },

      // 订单管理
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },

      // 课时管理
      {
        path: 'lessons',
        name: 'LessonList',
        component: () => import('@/views/lesson/LessonList.vue'),
        meta: { title: '课时记录' }
      },
      {
        path: 'lesson/:id',
        name: 'LessonDetail',
        component: () => import('@/views/lesson/LessonDetail.vue'),
        meta: { title: '课时详情' }
      },
      {
        path: 'checkin/:orderId',
        name: 'CheckIn',
        component: () => import('@/views/lesson/CheckIn.vue'),
        meta: { title: '上课打卡' }
      },

      // 钱包
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/wallet/Wallet.vue'),
        meta: { title: '我的钱包' }
      },
      {
        path: 'withdraw',
        name: 'Withdraw',
        component: () => import('@/views/wallet/Withdraw.vue'),
        meta: { title: '提现' }
      },

      // 聊天
      {
        path: 'chat',
        name: 'ChatList',
        component: () => import('@/views/chat/ChatList.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'chat/:id',
        name: 'ChatRoom',
        component: () => import('@/views/chat/ChatRoom.vue'),
        meta: { title: '私聊' }
      },

      // AI助手
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('@/views/ai/AiChat.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: 'ai/hub',
        name: 'AiHub',
        component: () => import('@/views/ai/AiHub.vue'),
        meta: { title: 'AI工具中心' }
      },
      {
        path: 'ai/lesson-plan',
        name: 'AiLessonPlan',
        component: () => import('@/views/ai/AiLessonPlan.vue'),
        meta: { title: 'AI课程规划' }
      },
      {
        path: 'ai/comment-polish',
        name: 'AiCommentPolish',
        component: () => import('@/views/ai/AiCommentPolish.vue'),
        meta: { title: 'AI评语润色' }
      },

      // 个人中心
      {
        path: 'mine',
        name: 'Mine',
        component: () => import('@/views/mine/Mine.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/mine/Settings.vue'),
        meta: { title: '设置' }
      }
    ]
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '素质教育平台'} - 教师端`
  
  // 检查登录状态
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (token && userRole !== 'tutor' && to.path !== '/login' && to.path !== '/register') {
    // 非教师用户访问教师端，提示并跳转
    next('/login')
  } else {
    next()
  }
})

export default router
