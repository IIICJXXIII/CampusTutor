/**
 * 课时相关 API (共享模块)
 */
import request from './request';

/**
 * 上课打卡 (教师)
 * @param {Object} data - { orderId, longitude, latitude }
 */
export function checkIn(data) {
  return request.post('/teaching/check-in', data);
}

/**
 * 下课打卡 (教师)
 * @param {number} id - 课时记录ID
 * @param {Object} data - { longitude, latitude }
 */
export function checkOut(id, data) {
  return request.post(`/teaching/check-out/${id}`, data);
}

/**
 * 确认课时 (家长)
 * @param {number} id - 课时记录ID
 */
export function confirmLesson(id) {
  return request.post(`/teaching/confirm/${id}`);
}

/**
 * 申诉课时 (家长)
 * @param {number} id - 课时记录ID
 * @param {string} reason - 申诉原因
 */
export function disputeLesson(id, reason) {
  return request.post(`/teaching/dispute/${id}`, null, {
    params: { reason }
  });
}

/**
 * 获取我的课时记录
 * @param {Object} params - 分页参数
 */
export function getMyLessons(params) {
  return request.get('/teaching/my-records', { params });
}

/**
 * 获取订单课时记录
 * @param {number} orderId - 订单ID
 */
export function getOrderLessons(orderId) {
  return request.get(`/teaching/order/${orderId}`);
}

/**
 * 获取课时详情
 * @param {number} id - 课时记录ID
 */
export function getLessonDetail(id) {
  return request.get(`/teaching/${id}`);
}
