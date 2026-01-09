/**
 * 评价相关 API (共享模块)
 * 对应后端 ReviewController (待开发)
 */
import request from './request';

/**
 * 提交评价
 * @param {Object} data - { orderId, targetUserId, rating, content, tags }
 */
export function submitReview(data) {
  return request.post('/review', data);
}

/**
 * 获取订单评价
 * @param {number} orderId - 订单ID
 */
export function getOrderReview(orderId) {
  return request.get(`/review/order/${orderId}`);
}

/**
 * 获取用户收到的评价列表
 * @param {number} userId - 用户ID
 * @param {Object} params - 分页参数
 */
export function getUserReviews(userId, params) {
  return request.get(`/review/user/${userId}`, { params });
}

/**
 * 获取我发出的评价列表
 * @param {Object} params - 分页参数
 */
export function getMyReviews(params) {
  return request.get('/review/my', { params });
}

/**
 * 获取教师评价统计
 * @param {number} tutorId - 教师用户ID
 */
export function getTutorReviewStats(tutorId) {
  return request.get(`/review/stats/${tutorId}`);
}
