/**
 * 错题本相关 API (共享模块)
 * 对应后端 WrongBookController (待开发)
 */
import request from './request';

/**
 * 添加错题
 * @param {Object} data - { studentId, subject, imageUrl, content, answer, analysis }
 */
export function addWrongQuestion(data) {
  return request.post('/wrongbook', data);
}

/**
 * 更新错题
 * @param {Object} data - 错题信息
 */
export function updateWrongQuestion(data) {
  return request.put('/wrongbook', data);
}

/**
 * 删除错题
 * @param {number} id - 错题ID
 */
export function deleteWrongQuestion(id) {
  return request.delete(`/wrongbook/${id}`);
}

/**
 * 获取错题列表
 * @param {Object} params - { studentId, subject, page, size }
 */
export function getWrongQuestions(params) {
  return request.get('/wrongbook/list', { params });
}

/**
 * 获取错题详情
 * @param {number} id - 错题ID
 */
export function getWrongQuestionDetail(id) {
  return request.get(`/wrongbook/${id}`);
}

/**
 * 标记错题为已掌握
 * @param {number} id - 错题ID
 */
export function markAsMastered(id) {
  return request.post(`/wrongbook/${id}/master`);
}

/**
 * 获取错题统计
 * @param {number} studentId - 学生ID
 */
export function getWrongQuestionStats(studentId) {
  return request.get(`/wrongbook/stats/${studentId}`);
}
