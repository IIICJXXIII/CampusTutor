import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 用户信息 Store
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const userRole = ref(localStorage.getItem('userRole') || '') // 'parent' | 'tutor'

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isParent = computed(() => userRole.value === 'parent' || userInfo.value?.role === 2)
  const isTutor = computed(() => userRole.value === 'tutor' || userInfo.value?.role === 1)
  const userId = computed(() => userInfo.value?.id || null)
  const nickname = computed(() => userInfo.value?.nickname || '用户')
  const avatar = computed(() => userInfo.value?.avatar || 'https://api.dicebear.com/7.x/miniavs/svg?seed=default')

  // 方法
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    
    // 根据 role 设置用户类型
    if (info?.role === 1) {
      setRole('tutor')
    } else if (info?.role === 2) {
      setRole('parent')
    }
  }

  function setRole(role) {
    userRole.value = role
    localStorage.setItem('userRole', role)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
  }

  return {
    token,
    userInfo,
    userRole,
    isLoggedIn,
    isParent,
    isTutor,
    userId,
    nickname,
    avatar,
    setToken,
    setUserInfo,
    setRole,
    logout
  }
})

// 教师认证状态 Store
export const useTutorStore = defineStore('tutor', () => {
  const profile = ref(null)
  const certStatus = ref(0) // 0待提交 1待审核 2已通过 3已拒绝

  const isCertified = computed(() => certStatus.value === 2)
  const isPending = computed(() => certStatus.value === 1)

  function setProfile(data) {
    profile.value = data
    if (data?.certStatus !== undefined) {
      certStatus.value = data.certStatus
    }
  }

  function setCertStatus(status) {
    certStatus.value = status
  }

  return {
    profile,
    certStatus,
    isCertified,
    isPending,
    setProfile,
    setCertStatus
  }
})

// 订单 Store
export const useOrderStore = defineStore('order', () => {
  const orders = ref([])
  const currentOrder = ref(null)

  function setOrders(list) {
    orders.value = list
  }

  function addOrder(order) {
    orders.value.unshift(order)
  }

  function updateOrder(orderId, updates) {
    const index = orders.value.findIndex(o => o.id === orderId)
    if (index !== -1) {
      orders.value[index] = { ...orders.value[index], ...updates }
    }
  }

  function updateOrderStatus(orderId, status) {
    const index = orders.value.findIndex(o => o.id === orderId)
    if (index !== -1) {
      orders.value[index].status = status
      if (status === 'active') {
        orders.value[index].tags = ['进行中']
      } else if (status === 'completed') {
        orders.value[index].tags = ['已完成']
      }
    }
  }

  function setCurrentOrder(order) {
    currentOrder.value = order
  }

  return {
    orders,
    currentOrder,
    setOrders,
    addOrder,
    updateOrder,
    updateOrderStatus,
    setCurrentOrder
  }
})

// 需求 Store
export const useDemandStore = defineStore('demand', () => {
  const demands = ref([])
  const currentDemand = ref(null)

  function setDemands(list) {
    demands.value = list
  }

  function setCurrentDemand(demand) {
    currentDemand.value = demand
  }

  return {
    demands,
    currentDemand,
    setDemands,
    setCurrentDemand
  }
})
