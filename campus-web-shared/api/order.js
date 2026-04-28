/**
 * 订单相关 API (共享模块)
 */
import request from './request';

/**
 * 创建订单 (家长)
 * @param {Object} data - 订单信息
 */
export function createOrder(data) {
  return request.post('/order/create', data);
}

/**
 * 教师接单
 * @param {Object} data - { demandId, totalHours, remark }
 */
export function acceptOrder(data) {
  return request.post('/order/accept', data);
}

/**
 * 家长确认订单
 * @param {number} id - 订单ID
 */
export function confirmOrder(id) {
  return request.post(`/order/${id}/confirm`);
}

/**
 * 支付订单 (家长)
 * @param {Object} data - { orderId, payType }
 */
export function payOrder(data) {
  return request.post('/order/pay', data);
}

/**
 * 取消订单
 * @param {number} id - 订单ID
 * @param {string} reason - 取消原因
 */
export function cancelOrder(id, reason) {
  return request.post(`/order/${id}/cancel`, null, {
    params: { reason }
  });
}

/**
 * 教员确认开课
 * @param {number} id - 订单ID
 */
export function confirmStartOrder(id) {
  return request.post(`/order/${id}/start`);
}

/**
 * 教师确认预约订单
 * @param {number} id - 订单ID
 */
export function tutorConfirmOrder(id) {
  return request.post(`/order/${id}/tutor-confirm`);
}

/**
 * 教师拒绝预约订单
 * @param {number} id - 订单ID
 * @param {string} reason - 拒绝原因
 */
export function tutorRejectOrder(id, reason) {
  return request.post(`/order/${id}/tutor-reject`, null, {
    params: { reason }
  });
}

/**
 * 完成订单
 * @param {number} id - 订单ID
 */
export function completeOrder(id) {
  return request.post(`/order/${id}/complete`);
}

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 */
export function getOrderDetail(id) {
  return request.get(`/order/${id}`);
}

/**
 * 获取家长订单列表
 * @param {Object} params - 分页参数
 */
export function getParentOrders(params) {
  return request.get('/order/parent/list', { params });
}

/**
 * 获取教员订单列表
 * @param {Object} params - 分页参数
 */
export function getTutorOrders(params) {
  return request.get('/order/tutor/list', { params });
}

/**
 * 申请退款
 * @param {number} id - 订单ID
 * @param {string} reason - 退款原因
 */
export function refundOrder(id, reason) {
  return request.post(`/order/${id}/refund`, null, {
    params: { reason }
  });
}
