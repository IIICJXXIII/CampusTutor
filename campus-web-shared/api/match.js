/**
 * 匹配相关 API (共享模块)
 */
import request from './request';

/**
 * 高级搜索教员
 * @param {Object} data - 搜索条件
 */
export function searchTutors(data) {
  return request.post('/match/tutors', data);
}

/**
 * 简化搜索教员
 * @param {Object} params - 搜索参数
 */
export function getTutorList(params) {
  return request.get('/match/tutors', { params });
}
