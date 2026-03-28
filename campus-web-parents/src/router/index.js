import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@shared/stores'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      // 首页 - 找老师
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/FindTeachers.vue'),
        meta: { title: '找老师', tabbar: true }
      },
      {
        path: 'teachers',
        name: 'TeacherList',
        component: () => import('@/views/home/TeacherList.vue'),
        meta: { title: '教员列表' }
      },
      {
        path: 'teachers/:id',
        name: 'TeacherDetail',
        component: () => import('@/views/home/TeacherDetail.vue'),
        meta: { title: '教员详情' }
      },
      
      // 需求管理
      {
        path: 'demands',
        name: 'DemandList',
        component: () => import('@/views/demand/DemandList.vue'),
        meta: { title: '我的需求', tabbar: true }
      },
      {
        path: 'demands/create',
        name: 'CreateDemand',
        component: () => import('@/views/demand/CreateDemand.vue'),
        meta: { title: '发布需求' }
      },
      {
        path: 'demands/:id',
        name: 'DemandDetail',
        component: () => import('@/views/demand/DemandDetail.vue'),
        meta: { title: '需求详情' }
      },
      {
        path: 'demands/:id/edit',
        name: 'EditDemand',
        component: () => import('@/views/demand/EditDemand.vue'),
        meta: { title: '编辑需求' }
      },
      {
        path: 'demands/:id/applicants',
        name: 'ApplicantList',
        component: () => import('@/views/demand/ApplicantList.vue'),
        meta: { title: '申请列表' }
      },
      
      // 学生管理
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '我的孩子' }
      },
      {
        path: 'students/add',
        name: 'AddStudent',
        component: () => import('@/views/student/AddStudent.vue'),
        meta: { title: '添加孩子' }
      },
      {
        path: 'students/:id',
        name: 'StudentDetail',
        component: () => import('@/views/student/StudentDetail.vue'),
        meta: { title: '孩子详情' }
      },
      
      // 订单管理
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '我的订单', tabbar: true }
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'orders/:id/pay',
        name: 'OrderPay',
        component: () => import('@/views/order/OrderPay.vue'),
        meta: { title: '订单支付' }
      },
      {
        path: 'orders/:id/review',
        name: 'OrderReview',
        component: () => import('@/views/order/OrderReview.vue'),
        meta: { title: '评价订单' }
      },
      {
        path: 'orders/:id/sign',
        name: 'ElectronicSign',
        component: () => import('@/views/order/ElectronicSign.vue'),
        meta: { title: '电子合同签署' }
      },
      
      // 课程管理
      {
        path: 'lessons',
        name: 'LessonList',
        component: () => import('@/views/lesson/LessonList.vue'),
        meta: { title: '课程记录' }
      },
      {
        path: 'lessons/:id',
        name: 'LessonDetail',
        component: () => import('@/views/lesson/LessonDetail.vue'),
        meta: { title: '课程详情' }
      },
      {
        path: 'lessons/:id/confirm',
        name: 'ConfirmLesson',
        component: () => import('@/views/lesson/ConfirmLesson.vue'),
        meta: { title: '确认课程' }
      },
      
      // 错题本
      {
        path: 'wrongbook',
        name: 'WrongBook',
        component: () => import('@/views/wrongbook/WrongBook.vue'),
        meta: { title: '错题本' }
      },
      {
        path: 'wrongbook/add',
        name: 'AddWrongQuestion',
        component: () => import('@/views/wrongbook/AddQuestion.vue'),
        meta: { title: '添加错题' }
      },
      {
        path: 'wrongbook/:id',
        name: 'WrongQuestionDetail',
        component: () => import('@/views/wrongbook/QuestionDetail.vue'),
        meta: { title: '错题详情' }
      },
      
      // 钱包
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/wallet/Wallet.vue'),
        meta: { title: '我的钱包' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/wallet/Recharge.vue'),
        meta: { title: '充值' }
      },
      
      // 聊天
      {
        path: 'chat',
        name: 'ChatList',
        component: () => import('@/views/chat/ChatList.vue'),
        meta: { title: '消息' }
      },
      {
        path: 'chat/:id',
        name: 'ChatRoom',
        component: () => import('@/views/chat/ChatRoom.vue'),
        meta: { title: '聊天' }
      },
      
      // AI助手
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('@/views/ai/AiChat.vue'),
        meta: { title: 'AI助手' }
      },
      
      // 个人中心
      {
        path: 'mine',
        name: 'Mine',
        component: () => import('@/views/mine/Mine.vue'),
        meta: { title: '我的', tabbar: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '设置' }
      },
      {
        path: 'settings/profile',
        name: 'EditProfile',
        component: () => import('@/views/settings/EditProfile.vue'),
        meta: { title: '编辑资料' }
      }
    ]
  },
  // 登录注册
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 素质教育平台` : '素质教育平台'
  
  // 白名单
  const whiteList = ['/login', '/register']
  
  const userStore = useUserStore()
  
  if (userStore.token) {
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
