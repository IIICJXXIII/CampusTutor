/**
 * 状态映射工具 (共享模块)
 * 统一管理订单、需求、课时、提现等状态的显示文本和 Element-Plus Tag 类型
 */

// ==================== 订单状态 ====================

const ORDER_STATUS_TYPE = {
  '-1': 'warning',
  0: 'danger',
  1: 'success',
  2: 'primary',
  3: 'success',
  4: 'info',
  5: 'warning',
  6: 'info'
}

const ORDER_STATUS_TEXT = {
  '-1': '待确认',
  0: '待支付',
  1: '待开课',
  2: '进行中',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款'
}

export const getOrderStatusType = (status) => ORDER_STATUS_TYPE[status] || 'info'
export const getOrderStatusText = (status) => ORDER_STATUS_TEXT[status] || '未知'

// ==================== 需求状态 ====================

const DEMAND_STATUS_TYPE = {
  0: 'info',
  1: 'success',
  2: 'warning',
  3: ''
}

const DEMAND_STATUS_TEXT = {
  0: '草稿',
  1: '已上架',
  2: '已下架',
  3: '已完成'
}

export const getDemandStatusType = (status) => DEMAND_STATUS_TYPE[status] || 'info'
export const getDemandStatusText = (status) => DEMAND_STATUS_TEXT[status] || '未知'

// ==================== 课时状态 ====================

const LESSON_STATUS_TYPE = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const LESSON_STATUS_TEXT = {
  0: '上课中',
  1: '待确认',
  2: '已确认',
  3: '申诉中'
}

export const getLessonStatusType = (status) => LESSON_STATUS_TYPE[status] || 'info'
export const getLessonStatusText = (status) => LESSON_STATUS_TEXT[status] || '未知'

// ==================== 提现状态 ====================

const WITHDRAW_STATUS_TYPE = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const WITHDRAW_STATUS_TEXT = {
  0: '审核中',
  1: '处理中',
  2: '已到账',
  3: '已拒绝'
}

export const getWithdrawStatusType = (status) => WITHDRAW_STATUS_TYPE[status] || 'info'
export const getWithdrawStatusText = (status) => WITHDRAW_STATUS_TEXT[status] || '未知'

// ==================== 教师认证状态 ====================

const CERT_STATUS_TYPE = {
  0: 'info',
  1: 'warning',
  2: 'success',
  3: 'danger'
}

const CERT_STATUS_TEXT = {
  0: '待提交',
  1: '待审核',
  2: '已通过',
  3: '已拒绝'
}

export const getCertStatusType = (status) => CERT_STATUS_TYPE[status] || 'info'
export const getCertStatusText = (status) => CERT_STATUS_TEXT[status] || '未知'
