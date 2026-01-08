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
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },

  // === 家长端 ===
  {
    path: '/parent/demand',
    name: 'ParentDemand',
    component: () => import('@/views/Parent/DemandForm.vue'),
    meta: { title: '发布需求', role: 'parent' }
  },
  {
    path: '/parent/tutors',
    name: 'ParentTutorList',
    component: () => import('@/views/Parent/TutorList.vue'),
    meta: { title: '匹配老师', role: 'parent' }
  },
  {
    path: '/teacher/list',
    name: 'TeacherList',
    component: () => import('@/views/Teacher/TeacherList.vue'),
    meta: { title: '找老师' }
  },
  {
    path: '/teacher/:id',
    name: 'TeacherProfile',
    component: () => import('@/views/Teacher/TeacherProfile.vue'),
    meta: { title: '教师详情' }
  },
  {
    path: '/booking/:teacherId',
    name: 'Booking',
    component: () => import('@/views/Parent/Booking.vue'),
    meta: { title: '预约签约', role: 'parent' }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/Parent/Payment.vue'),
    meta: { title: '支付', role: 'parent' }
  },
  {
    path: '/parent/wrong-book',
    name: 'WrongBook',
    component: () => import('@/views/Parent/WrongBook.vue'),
    meta: { title: '错题本', role: 'parent' }
  },

  // === 教师端 ===
  {
    path: '/teacher/auth',
    name: 'TeacherAuth',
    component: () => import('@/views/Teacher/TeacherAuth.vue'),
    meta: { title: '资质认证', role: 'tutor' }
  },
  {
    path: '/teacher/resume',
    name: 'MyResume',
    component: () => import('@/views/Teacher/MyResume.vue'),
    meta: { title: '我的简历', role: 'tutor' }
  },
  {
    path: '/teacher/students',
    name: 'FindStudents',
    component: () => import('@/views/Teacher/FindStudents.vue'),
    meta: { title: '找学生', role: 'tutor' }
  },
  {
    path: '/find-students',
    redirect: '/teacher/students'
  },
  {
    path: '/student/:id',
    name: 'StudentDetail',
    component: () => import('@/views/Teacher/StudentDetail.vue'),
    meta: { title: '学生详情', role: 'tutor' }
  },

  // === 公共模块 ===
  {
    path: '/process/record',
    name: 'ClassRecord',
    component: () => import('@/views/Process/ClassRecord.vue'),
    meta: { title: '课时记录' }
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('@/views/Mine/Mine.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Mine/Settings.vue'),
    meta: { title: '设置' }
  },
  {
    path: '/mine/orders',
    name: 'OrderList',
    component: () => import('@/views/Mine/OrderList.vue'),
    meta: { title: '我的订单' }
  },

  // 钱包模块
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Mine/Wallet.vue'),
    meta: { title: '我的钱包' }
  },

  {
    path: '/wallet/withdraw',
    name: 'Withdraw',
    component: () => import('@/views/Mine/Withdraw.vue'),
    meta: { title: '提现' }
  },

  // === 聊天模块 ===
  {
    path: '/chat',
    name: 'ChatList',
    component: () => import('@/views/Chat/Chat.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/chat/:targetUserId',
    name: 'ChatRoom',
    component: () => import('@/views/Chat/Chat.vue'),
    meta: { title: '私聊', requiresAuth: true }
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  },
  {
    path: '/service/chat',
    name: 'AiChat',
    component: () => import('@/views/Service/AiChat.vue'),
    meta: {
      title: 'AI智能助手',
      requiresAuth: true // 确保只有登录后才能访问
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 白名单路由
const whiteList = ['/login', '/register']

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园智教` : '校园智教'

  const token = localStorage.getItem('token')

  if (whiteList.includes(to.path)) {
    if (token && to.path === '/login') {
      const userRole = localStorage.getItem('userRole')
      next(userRole === 'tutor' ? '/teacher/students' : '/teacher/list')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router