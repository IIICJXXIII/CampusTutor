/**
 * 共享 Store 模块 (Pinia)
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户信息 Store
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const userRole = ref(localStorage.getItem('userRole') || '') // 'parent' | 'tutor'

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isParent = computed(() => userRole.value === 'parent')
  const isTutor = computed(() => userRole.value === 'tutor')
  const userId = computed(() => userInfo.value?.id || null)
  const nickname = computed(() => userInfo.value?.nickname || '用户')
  const avatar = computed(() => userInfo.value?.avatar || 'https://api.dicebear.com/7.x/miniavs/svg?seed=default')
  const phone = computed(() => userInfo.value?.phone || '')

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
    phone,
    setToken,
    setUserInfo,
    setRole,
    logout
  }
})

/**
 * 教师认证状态 Store
 */
export const useTutorStore = defineStore('tutor', () => {
  const profile = ref(null)
  const certStatus = ref(0) // 0待提交 1待审核 2已通过 3已拒绝

  const isCertified = computed(() => certStatus.value === 2)
  const isPending = computed(() => certStatus.value === 1)
  const isRejected = computed(() => certStatus.value === 3)

  function setProfile(data) {
    profile.value = data
    if (data?.certStatus !== undefined) {
      certStatus.value = data.certStatus
    }
  }

  function setCertStatus(status) {
    certStatus.value = status
  }

  function reset() {
    profile.value = null
    certStatus.value = 0
  }

  return {
    profile,
    certStatus,
    isCertified,
    isPending,
    isRejected,
    setProfile,
    setCertStatus,
    reset
  }
})

/**
 * 订单 Store
 */
export const useOrderStore = defineStore('order', () => {
  const orders = ref([])
  const currentOrder = ref(null)
  const loading = ref(false)

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
    }
  }

  function setCurrentOrder(order) {
    currentOrder.value = order
  }

  function reset() {
    orders.value = []
    currentOrder.value = null
  }

  return {
    orders,
    currentOrder,
    loading,
    setOrders,
    addOrder,
    updateOrder,
    updateOrderStatus,
    setCurrentOrder,
    reset
  }
})

/**
 * 需求 Store
 */
export const useDemandStore = defineStore('demand', () => {
  const demands = ref([])
  const currentDemand = ref(null)
  const loading = ref(false)

  function setDemands(list) {
    demands.value = list
  }

  function setCurrentDemand(demand) {
    currentDemand.value = demand
  }

  function addDemand(demand) {
    demands.value.unshift(demand)
  }

  function updateDemand(demandId, updates) {
    const index = demands.value.findIndex(d => d.id === demandId)
    if (index !== -1) {
      demands.value[index] = { ...demands.value[index], ...updates }
    }
  }

  function removeDemand(demandId) {
    const index = demands.value.findIndex(d => d.id === demandId)
    if (index !== -1) {
      demands.value.splice(index, 1)
    }
  }

  function reset() {
    demands.value = []
    currentDemand.value = null
  }

  return {
    demands,
    currentDemand,
    loading,
    setDemands,
    setCurrentDemand,
    addDemand,
    updateDemand,
    removeDemand,
    reset
  }
})

/**
 * 钱包 Store
 */
export const useWalletStore = defineStore('wallet', () => {
  const balance = ref(0)
  const frozenBalance = ref(0)
  const totalIncome = ref(0)
  const transactions = ref([])

  function setWalletInfo(data) {
    balance.value = data.balance || 0
    frozenBalance.value = data.frozenBalance || 0
    totalIncome.value = data.totalIncome || 0
  }

  function setTransactions(list) {
    transactions.value = list
  }

  function reset() {
    balance.value = 0
    frozenBalance.value = 0
    totalIncome.value = 0
    transactions.value = []
  }

  return {
    balance,
    frozenBalance,
    totalIncome,
    transactions,
    setWalletInfo,
    setTransactions,
    reset
  }
})

/**
 * 聊天 Store
 */
export const useChatStore = defineStore('chat', () => {
  const conversations = ref([])
  const currentChat = ref(null)
  const messages = ref([])
  const unreadCount = ref(0)

  function setConversations(list) {
    conversations.value = list
  }

  function setCurrentChat(chat) {
    currentChat.value = chat
  }

  function setMessages(list) {
    messages.value = list
  }

  function addMessage(message) {
    messages.value.push(message)
  }

  function setUnreadCount(count) {
    unreadCount.value = count
  }

  function reset() {
    conversations.value = []
    currentChat.value = null
    messages.value = []
    unreadCount.value = 0
  }

  return {
    conversations,
    currentChat,
    messages,
    unreadCount,
    setConversations,
    setCurrentChat,
    setMessages,
    addMessage,
    setUnreadCount,
    reset
  }
})
