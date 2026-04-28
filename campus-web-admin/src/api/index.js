import request from './request'

// ==================== 认证相关 ====================
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getProfile: () => request.get('/auth/profile')
}

// ==================== 用户管理 ====================
export const userApi = {
  getList: (params) => request.get('/admin/users', { params }),
  getById: (id) => request.get(`/admin/users/${id}`),
  update: (id, data) => request.put(`/admin/users/${id}`, data),
  updateStatus: (id, status) => request.put(`/admin/users/${id}/status`, { status }),
  delete: (id) => request.delete(`/admin/users/${id}`)
}

// ==================== 教师管理 ====================
export const tutorApi = {
  getList: (params) => request.get('/admin/tutors', { params }),
  getById: (id) => request.get(`/admin/tutors/${id}`),
  update: (id, data) => request.put(`/admin/tutors/${id}`, data),
  // 认证审核
  getPendingList: (params) => request.get('/admin/tutors/pending', { params }),
  approve: (id) => request.post(`/admin/tutors/${id}/approve`),
  reject: (id, reason) => request.post(`/admin/tutors/${id}/reject`, { reason })
}

// ==================== 家长管理 ====================
export const parentApi = {
  getList: (params) => request.get('/admin/parents', { params }),
  getById: (id) => request.get(`/admin/parents/${id}`),
  update: (id, data) => request.put(`/admin/parents/${id}`, data)
}

// ==================== 需求管理 ====================
export const demandApi = {
  getList: (params) => request.get('/admin/demands', { params }),
  getById: (id) => request.get(`/admin/demands/${id}`),
  update: (id, data) => request.put(`/admin/demands/${id}`, data),
  updateStatus: (id, status) => request.put(`/admin/demands/${id}/status`, { status }),
  delete: (id) => request.delete(`/admin/demands/${id}`)
}

// ==================== 订单管理 ====================
export const orderApi = {
  getList: (params) => request.get('/admin/orders', { params }),
  getById: (id) => request.get(`/admin/orders/${id}`),
  updateStatus: (id, status) => request.put(`/admin/orders/${id}/status`, { status }),
  // 资金操作
  releaseEscrow: (id) => request.post(`/admin/orders/${id}/release`),
  refund: (id, data) => request.post(`/admin/orders/${id}/refund`, data)
}

// ==================== 课时管理 ====================
export const lessonApi = {
  getList: (params) => request.get('/admin/lessons', { params }),
  getById: (id) => request.get(`/admin/lessons/${id}`),
  confirm: (id) => request.post(`/admin/lessons/${id}/confirm`),
  reject: (id, reason) => request.post(`/admin/lessons/${id}/reject`, { reason })
}

// ==================== 匹配记录 ====================
export const matchApi = {
  getList: (params) => request.get('/admin/matches', { params }),
  getById: (id) => request.get(`/admin/matches/${id}`)
}

// ==================== 钱包管理 ====================
export const walletApi = {
  getList: (params) => request.get('/admin/wallets', { params }),
  getById: (id) => request.get(`/admin/wallets/${id}`),
  getTransactions: (id, params) => request.get(`/admin/wallets/${id}/transactions`, { params }),
  adjust: (id, data) => request.post(`/admin/wallets/${id}/adjust`, data)
}

// ==================== 统计数据 ====================
export const statsApi = {
  getDashboard: () => request.get('/admin/stats/dashboard'),
  getUserStats: (params) => request.get('/admin/stats/users', { params }),
  getOrderStats: (params) => request.get('/admin/stats/orders', { params }),
  getRevenueStats: (params) => request.get('/admin/stats/revenue', { params })
}

// ==================== 系统设置 ====================
export const settingsApi = {
  getSettings: () => request.get('/admin/settings'),
  updateSettings: (data) => request.put('/admin/settings', data)
}
