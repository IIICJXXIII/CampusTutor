import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' }, // 默认去登录页
  { path: '/login', component: () => import('../views/Login.vue') },
  
  // 家长端
  { path: '/parent/demand', component: () => import('../views/Parent/DemandForm.vue') },
  { path: '/teacher/list', component: () => import('../views/Teacher/TeacherList.vue') }, // 家长找老师
  {
    path: '/teacher/:id',
    name: 'TeacherProfile',
    component: () => import('../views/Teacher/TeacherProfile.vue')
  },
  // 新增：预约签约页 [cite: 89]
  {
    path: '/booking/:teacherId',
    name: 'Booking',
    component: () => import('../views/Parent/Booking.vue')
  },

  // 教师端 (新)
  { path: '/teacher/students', component: () => import('../views/Teacher/StudentList.vue') }, // 老师找学生
  { 
    path: '/teacher/resume', 
    component: () => import('../views/Teacher/MyResume.vue') 
  },
  
  // 2. 学生需求详情 (点击列表进入)
  { 
    path: '/teacher/resume', 
    name: 'MyResume', // 确保名字对应
    component: () => import('../views/Teacher/MyResume.vue') 
  },
  { 
    path: '/teacher/resume', 
    name: 'TeacherAuth', 
    component: () => import('../views/Teacher/TeacherAuth.vue') 
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('../views/Parent/Payment.vue')
  },
  // 课时记录 (确保路径对)
  {
    path: '/process/record',
    name: 'ClassRecord',
    component: () => import('../views/Process/ClassRecord.vue')
  },
  {
    path: '/mine/orders',
    name: 'OrderList',
    component: () => import('../views/Mine/OrderList.vue')
  },
  // 错题本 (家长端功能)
  {
    path: '/parent/wrong-book',
    name: 'WrongBook',
    component: () => import('../views/Parent/WrongBook.vue')
  },
  {
    path: '/teacher/students',
    name: 'FindStudents',
    component: () => import('../views/Teacher/FindStudents.vue')
  },
  // 教师端：学生需求详情
  {
    path: '/student/:id',
    name: 'StudentDetail',
    component: () => import('../views/Teacher/StudentDetail.vue')
  },
  // 公共
  { path: '/mine', component: () => import('../views/Mine/Mine.vue') },
  { path: '/process/record', component: () => import('../views/Process/ClassRecord.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
export default router