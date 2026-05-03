/**
 * 统一路由配置
 * 合并家长端 + 教师端，按角色分区
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@shared/stores'

const routes = [
  // ===================== 公共页面 (无需登录) =====================
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

  // ===================== 主布局 (需要登录) =====================
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // ============== 家长端路由 ==============
      {
        path: 'parent/home',
        name: 'ParentHome',
        component: () => import('@/views/parent/home/FindTeachers.vue'),
        meta: { title: '找老师', role: 'parent', tabbar: true }
      },
      {
        path: 'parent/teachers',
        name: 'ParentTeacherList',
        component: () => import('@/views/parent/home/TeacherList.vue'),
        meta: { title: '教员列表', role: 'parent' }
      },
      {
        path: 'parent/teachers/:id',
        name: 'ParentTeacherDetail',
        component: () => import('@/views/parent/home/TeacherDetail.vue'),
        meta: { title: '教员详情', role: 'parent' }
      },

      // 需求管理
      {
        path: 'parent/demands',
        name: 'ParentDemandList',
        component: () => import('@/views/parent/demand/DemandList.vue'),
        meta: { title: '我的需求', role: 'parent', tabbar: true }
      },
      {
        path: 'parent/demands/create',
        name: 'ParentCreateDemand',
        component: () => import('@/views/parent/demand/CreateDemand.vue'),
        meta: { title: '发布需求', role: 'parent' }
      },
      {
        path: 'parent/demands/:id',
        name: 'ParentDemandDetail',
        component: () => import('@/views/parent/demand/DemandDetail.vue'),
        meta: { title: '需求详情', role: 'parent' }
      },
      {
        path: 'parent/demands/:id/edit',
        name: 'ParentEditDemand',
        component: () => import('@/views/parent/demand/EditDemand.vue'),
        meta: { title: '编辑需求', role: 'parent' }
      },
      {
        path: 'parent/demands/:id/applicants',
        name: 'ParentApplicantList',
        component: () => import('@/views/parent/demand/ApplicantList.vue'),
        meta: { title: '申请列表', role: 'parent' }
      },

      // 学生管理
      {
        path: 'parent/students',
        name: 'ParentStudentList',
        component: () => import('@/views/parent/student/StudentList.vue'),
        meta: { title: '我的孩子', role: 'parent' }
      },
      {
        path: 'parent/students/add',
        name: 'ParentAddStudent',
        component: () => import('@/views/parent/student/AddStudent.vue'),
        meta: { title: '添加孩子', role: 'parent' }
      },
      {
        path: 'parent/students/:id/edit',
        name: 'ParentEditStudent',
        component: () => import('@/views/parent/student/AddStudent.vue'),
        meta: { title: '编辑孩子', role: 'parent' }
      },
      {
        path: 'parent/students/:id',
        name: 'ParentStudentDetail',
        component: () => import('@/views/parent/student/StudentDetail.vue'),
        meta: { title: '孩子详情', role: 'parent' }
      },

      // 订单管理
      {
        path: 'parent/orders',
        name: 'ParentOrderList',
        component: () => import('@/views/parent/order/OrderList.vue'),
        meta: { title: '我的订单', role: 'parent', tabbar: true }
      },
      {
        path: 'parent/orders/:id',
        name: 'ParentOrderDetail',
        component: () => import('@/views/parent/order/OrderDetail.vue'),
        meta: { title: '订单详情', role: 'parent' }
      },
      {
        path: 'parent/orders/:id/pay',
        name: 'ParentOrderPay',
        component: () => import('@/views/parent/order/OrderPay.vue'),
        meta: { title: '订单支付', role: 'parent' }
      },
      {
        path: 'parent/orders/:id/review',
        name: 'ParentOrderReview',
        component: () => import('@/views/parent/order/OrderReview.vue'),
        meta: { title: '评价订单', role: 'parent' }
      },
      {
        path: 'parent/orders/:id/sign',
        name: 'ParentElectronicSign',
        component: () => import('@/views/parent/order/ElectronicSign.vue'),
        meta: { title: '电子合同签署', role: 'parent' }
      },

      // 课程管理
      {
        path: 'parent/lessons',
        name: 'ParentLessonList',
        component: () => import('@/views/parent/lesson/LessonList.vue'),
        meta: { title: '课程记录', role: 'parent' }
      },
      {
        path: 'parent/lessons/:id',
        name: 'ParentLessonDetail',
        component: () => import('@/views/parent/lesson/LessonDetail.vue'),
        meta: { title: '课程详情', role: 'parent' }
      },
      {
        path: 'parent/lessons/:id/confirm',
        name: 'ParentConfirmLesson',
        component: () => import('@/views/parent/lesson/ConfirmLesson.vue'),
        meta: { title: '确认课程', role: 'parent' }
      },

      // 错题本
      {
        path: 'parent/wrongbook',
        name: 'ParentWrongBook',
        component: () => import('@/views/parent/wrongbook/WrongBook.vue'),
        meta: { title: '错题本', role: 'parent' }
      },
      {
        path: 'parent/wrongbook/add',
        name: 'ParentAddWrongQuestion',
        component: () => import('@/views/parent/wrongbook/AddQuestion.vue'),
        meta: { title: '添加错题', role: 'parent' }
      },
      {
        path: 'parent/wrongbook/:id',
        name: 'ParentWrongQuestionDetail',
        component: () => import('@/views/parent/wrongbook/QuestionDetail.vue'),
        meta: { title: '错题详情', role: 'parent' }
      },

      // 家长钱包
      {
        path: 'parent/wallet',
        name: 'ParentWallet',
        component: () => import('@/views/parent/wallet/Wallet.vue'),
        meta: { title: '我的钱包', role: 'parent' }
      },
      {
        path: 'parent/recharge',
        name: 'ParentRecharge',
        component: () => import('@/views/parent/wallet/Recharge.vue'),
        meta: { title: '充值', role: 'parent' }
      },

      // 学生报告
      {
        path: 'parent/reports',
        name: 'ParentReportList',
        component: () => import('@/views/parent/report/ReportList.vue'),
        meta: { title: '学生报告', role: 'parent' }
      },
      {
        path: 'parent/reports/:id',
        name: 'ParentReportDetail',
        component: () => import('@/views/parent/report/ReportDetail.vue'),
        meta: { title: '报告详情', role: 'parent' }
      },

      // ============== 教师端路由 ==============
      {
        path: 'teacher/home',
        name: 'TeacherHome',
        component: () => import('@/views/teacher/home/FindStudents.vue'),
        meta: { title: '找学生', role: 'tutor', tabbar: true }
      },
      {
        path: 'teacher/students',
        name: 'TeacherStudentList',
        component: () => import('@/views/teacher/home/StudentList.vue'),
        meta: { title: '学生列表', role: 'tutor' }
      },
      {
        path: 'teacher/demand/:id',
        name: 'TeacherDemandDetail',
        component: () => import('@/views/teacher/home/DemandDetail.vue'),
        meta: { title: '需求详情', role: 'tutor' }
      },

      // 资质认证
      {
        path: 'teacher/auth',
        name: 'TeacherAuth',
        component: () => import('@/views/teacher/certification/TeacherAuth.vue'),
        meta: { title: '资质认证', role: 'tutor' }
      },

      // 简历
      {
        path: 'teacher/resume',
        name: 'TeacherResume',
        component: () => import('@/views/teacher/resume/MyResume.vue'),
        meta: { title: '我的简历', role: 'tutor' }
      },
      {
        path: 'teacher/schedule',
        name: 'TeacherSchedule',
        component: () => import('@/views/teacher/resume/Schedule.vue'),
        meta: { title: '排课设置', role: 'tutor' }
      },

      // 教师订单
      {
        path: 'teacher/orders',
        name: 'TeacherOrderList',
        component: () => import('@/views/teacher/order/OrderList.vue'),
        meta: { title: '我的订单', role: 'tutor', tabbar: true }
      },
      {
        path: 'teacher/orders/:id',
        name: 'TeacherOrderDetail',
        component: () => import('@/views/teacher/order/OrderDetail.vue'),
        meta: { title: '订单详情', role: 'tutor' }
      },

      // 教师课时
      {
        path: 'teacher/lessons',
        name: 'TeacherLessonList',
        component: () => import('@/views/teacher/lesson/LessonList.vue'),
        meta: { title: '课时记录', role: 'tutor' }
      },
      {
        path: 'teacher/lesson/:id',
        name: 'TeacherLessonDetail',
        component: () => import('@/views/teacher/lesson/LessonDetail.vue'),
        meta: { title: '课时详情', role: 'tutor' }
      },
      {
        path: 'teacher/checkin',
        name: 'TeacherCheckIn',
        component: () => import('@/views/teacher/lesson/CheckIn.vue'),
        meta: { title: '上课打卡', role: 'tutor' }
      },

      // 教师钱包
      {
        path: 'teacher/wallet',
        name: 'TeacherWallet',
        component: () => import('@/views/teacher/wallet/Wallet.vue'),
        meta: { title: '我的钱包', role: 'tutor' }
      },
      {
        path: 'teacher/withdraw',
        name: 'TeacherWithdraw',
        component: () => import('@/views/teacher/wallet/Withdraw.vue'),
        meta: { title: '提现', role: 'tutor' }
      },

      // 教师AI工具
      {
        path: 'teacher/ai/hub',
        name: 'TeacherAiHub',
        component: () => import('@/views/teacher/ai/AiHub.vue'),
        meta: { title: 'AI工具中心', role: 'tutor' }
      },
      {
        path: 'teacher/ai/lesson-plan',
        name: 'TeacherAiLessonPlan',
        component: () => import('@/views/teacher/ai/AiLessonPlan.vue'),
        meta: { title: 'AI课程规划', role: 'tutor' }
      },
      {
        path: 'teacher/ai/comment-polish',
        name: 'TeacherAiCommentPolish',
        component: () => import('@/views/teacher/ai/AiCommentPolish.vue'),
        meta: { title: 'AI评语润色', role: 'tutor' }
      },

      // ============== 公共路由 (两种角色都可访问) ==============
      {
        path: 'chat',
        name: 'ChatList',
        component: () => import('@/views/common/chat/ChatList.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'chat/:id',
        name: 'ChatRoom',
        component: () => import('@/views/common/chat/ChatRoom.vue'),
        meta: { title: '私聊' }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/common/user/UserProfile.vue'),
        meta: { title: '用户资料' }
      },
      {
        path: 'ai',
        name: 'AiChat',
        component: () => import('@/views/common/ai/AiChat.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: 'community',
        name: 'CommunityList',
        component: () => import('@/views/common/community/CommunityList.vue'),
        meta: { title: '社区' }
      },
      {
        path: 'community/:id',
        name: 'CommunityDetail',
        component: () => import('@/views/common/community/CommunityDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'insurance',
        name: 'InsuranceList',
        component: () => import('@/views/common/insurance/InsuranceList.vue'),
        meta: { title: '保险单' }
      },
      {
        path: 'mine',
        name: 'Mine',
        component: () => import('@/views/common/mine/Mine.vue'),
        meta: { title: '个人中心', tabbar: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/common/settings/Settings.vue'),
        meta: { title: '设置' }
      },
      {
        path: 'settings/profile',
        name: 'EditProfile',
        component: () => import('@/views/common/settings/EditProfile.vue'),
        meta: { title: '编辑资料' }
      },
      {
        path: 'settings/password',
        name: 'ChangePassword',
        component: () => import('@/views/common/settings/ChangePassword.vue'),
        meta: { title: '修改密码' }
      },
      {
        path: 'settings/privacy',
        name: 'PrivacyPolicy',
        component: () => import('@/views/common/settings/PrivacyPolicy.vue'),
        meta: { title: '隐私政策' }
      },
      {
        path: 'settings/agreement',
        name: 'UserAgreement',
        component: () => import('@/views/common/settings/UserAgreement.vue'),
        meta: { title: '用户协议' }
      }
    ]
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/error/NotFound.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
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
  document.title = to.meta.title ? `${to.meta.title} - 校园智教` : '校园智教'

  const userStore = useUserStore()
  const token = userStore.token
  const userRole = userStore.userRole

  if (to.meta.requiresAuth === false) {
    if (token && (to.path === '/login' || to.path === '/register')) {
      if (userRole === 'tutor') {
        next('/teacher/home')
      } else if (userRole === 'parent') {
        next('/parent/home')
      } else {
        userStore.logout()
        next()
      }
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (!userRole) {
    userStore.logout()
    next('/login')
    return
  }

  if (to.path === '/') {
    next(userRole === 'tutor' ? '/teacher/home' : '/parent/home')
    return
  }

  const pathRole = to.path.startsWith('/teacher') ? 'tutor'
    : to.path.startsWith('/parent') ? 'parent'
    : null

  if (pathRole && pathRole !== userRole) {
    next(userRole === 'tutor' ? '/teacher/home' : '/parent/home')
    return
  }

  if (to.meta.role && to.meta.role !== userRole) {
    next(userRole === 'tutor' ? '/teacher/home' : '/parent/home')
    return
  }

  next()
})

export default router