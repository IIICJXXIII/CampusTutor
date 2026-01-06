/**
 * 课时打卡相关 API
 */
import request from './request';

/**
 * 教师打卡上课
 * @param {Object} data - { orderId, latitude, longitude, photoUrl, contentSummary, homeworkAssigned }
 */
export function checkIn(data) {
  return request.post('/teaching/check-in', data);
}

/**
 * 教师打卡下课
 * @param {number} recordId - 课时记录ID
 * @param {string} contentSummary - 教学内容摘要
 * @param {string} homeworkAssigned - 布置作业
 */
export function checkOut(recordId, contentSummary, homeworkAssigned) {
  return request.post(`/teaching/check-out/${recordId}`, null, {
    params: { contentSummary, homeworkAssigned }
  });
}

/**
 * 家长确认课时
 * @param {number} recordId - 课时记录ID
 */
export function confirmLesson(recordId) {
  return request.post(`/teaching/confirm/${recordId}`);
}

/**
 * 家长申诉课时
 * @param {number} recordId - 课时记录ID
 * @param {string} reason - 申诉原因
 */
export function disputeLesson(recordId, reason) {
  return request.post(`/teaching/dispute/${recordId}`, null, {
    params: { reason }
  });
}

/**
 * 获取订单课时记录
 * @param {number} orderId - 订单ID
 */
export function getTeachingRecords(orderId) {
  return request.get(`/teaching/records/${orderId}`);
}

/**
 * 获取课时记录详情
 * @param {number} recordId - 课时记录ID
 */
export function getTeachingRecord(recordId) {
  return request.get(`/teaching/record/${recordId}`);
}
