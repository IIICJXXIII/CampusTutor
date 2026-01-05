import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' }, // 默认去登录页
  { path: '/login', component: () => import('../views/Login.vue') },
  
  // 家长端
  { path: '/parent/demand', component: () => import('../views/Parent/DemandForm.vue') },
  { path: '/teacher/list', component: () => import('../views/Teacher/TeacherList.vue') }, // 家长找老师
  { path: '/teacher/:id', component: () => import('../views/Teacher/TeacherProfile.vue') },

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
  // 公共
  { path: '/mine', component: () => import('../views/Mine/Mine.vue') },
  { path: '/process/record', component: () => import('../views/Process/ClassRecord.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})
export default router