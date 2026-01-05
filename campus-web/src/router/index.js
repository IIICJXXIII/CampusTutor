import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // 1. 默认跳转
  {
    path: '/',
    redirect: '/login'
  },
  // 2. 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },

  // === 家长端核心流程 ===
  {
    path: '/parent/demand', // 发布需求
    name: 'ParentDemand',
    component: () => import('../views/Parent/DemandForm.vue')
  },
  {
    path: '/teacher/list', // 找老师列表 (AI匹配)
    name: 'TeacherList',
    component: () => import('../views/Teacher/TeacherList.vue')
  },
  {
    path: '/teacher/:id', // 教师详情页
    name: 'TeacherProfile',
    component: () => import('../views/Teacher/TeacherProfile.vue')
  },
  {
    path: '/booking/:teacherId', // 预约签约页
    name: 'Booking',
    component: () => import('../views/Parent/Booking.vue')
  },
  {
    path: '/payment', // 支付收银台
    name: 'Payment',
    component: () => import('../views/Parent/Payment.vue')
  },
  {
    path: '/parent/wrong-book', // 智能错题本
    name: 'WrongBook',
    component: () => import('../views/Parent/WrongBook.vue')
  },

  // === 教师端核心流程 ===
  {
    path: '/teacher/auth', // 资质认证 (新教师必经)
    name: 'TeacherAuth',
    component: () => import('../views/Teacher/TeacherAuth.vue')
  },
  {
    path: '/teacher/resume', // 简历编辑 (已认证教师日常使用)
    name: 'MyResume',
    component: () => import('../views/Teacher/MyResume.vue')
  },
  {
    path: '/teacher/students', // 找学生 (地图/列表)
    name: 'FindStudents',
    component: () => import('../views/Teacher/FindStudents.vue')
  },
  {
    path: '/student/:id', // 学生需求详情
    name: 'StudentDetail',
    component: () => import('../views/Teacher/StudentDetail.vue')
  },

  // === 公共/管理模块 ===
  {
    path: '/process/record', // 课时记录 (双端通用)
    name: 'ClassRecord',
    component: () => import('../views/Process/ClassRecord.vue')
  },
  {
    path: '/mine', // 个人中心
    name: 'Mine',
    component: () => import('../views/Mine/Mine.vue')
  },
  {
    path: '/mine/orders', // 我的订单
    name: 'OrderList',
    component: () => import('../views/Mine/OrderList.vue')
  },
  
  // === 消息模块 (已注释，防止报错) ===
  // 只有当你创建了对应的 .vue 文件后，才能解开下面的注释
  /*
  { 
    path: '/messages', 
    name: 'MessageList', 
    component: () => import('../views/Common/MessageList.vue') 
  },
  { 
    path: '/message/:id', 
    name: 'ChatDetail', 
    component: () => import('../views/Common/ChatDetail.vue') 
  }
  */
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router