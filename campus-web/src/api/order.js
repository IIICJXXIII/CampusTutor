/**
 * 订单相关 API
 */
import request from './request';

/**
 * 创建订单
 * @param {Object} data - { demandId, tutorId, courseType, totalLessons, unitPrice }
 */
export function createOrder(data) {
  return request.post('/order', data);
}

/**
 * 获取家长订单列表
 */
export function getParentOrders() {
  return request.get('/order/parent');
}

/**
 * 获取教员订单列表
 */
export function getTutorOrders() {
  return request.get('/order/tutor');
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
 * @param {number} id - 订单ID
 */
export function payOrder(id) {
  return request.post(`/order/${id}/pay`);
}

/**
 * 取消订单
 * @param {number} id - 订单ID
 * @param {string} reason - 取消原因
 */
export function cancelOrder(id, reason) {
  return request.post(`/order/${id}/cancel`, { reason });
}

/**
 * 完成订单
 * @param {number} id - 订单ID
 */
export function completeOrder(id) {
  return request.post(`/order/${id}/complete`);
}
