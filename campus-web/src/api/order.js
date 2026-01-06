/**
 * 订单相关 API
 */
import request from './request';

/**
 * 创建订单
 * @param {Object} data - { demandId, tutorId, courseType, totalLessons, unitPrice }
 */
export function createOrder(data) {
  return request.post('/order/create', data);
}

/**
 * 获取家长订单列表
 * @param {Object} params - { status, page, size }
 */
export function getParentOrders(params = {}) {
  return request.get('/order/parent/list', { params });
}

/**
 * 获取教员订单列表
 * @param {Object} params - { status, page, size }
 */
export function getTutorOrders(params = {}) {
  return request.get('/order/tutor/list', { params });
}

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 */
export function getOrderDetail(id) {
  return request.get(`/order/${id}`);
}

/**
 * 支付订单
 * @param {number} orderId - 订单ID
 * @param {string} payMethod - 支付方式
 */
export function payOrder(orderId, payMethod = 'WECHAT') {
  return request.post('/order/pay', { orderId, payMethod });
}

/**
 * 取消订单
 * @param {number} id - 订单ID
 * @param {string} reason - 取消原因
 */
export function cancelOrder(id, reason) {
  return request.post(`/order/${id}/cancel`, null, { params: { reason } });
}

/**
 * 完成订单
 * @param {number} id - 订单ID
 */
export function completeOrder(id) {
  return request.post(`/order/${id}/complete`);
}
