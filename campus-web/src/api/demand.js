/**
 * 需求相关 API
 */
import request from './request';

/**
 * 发布需求
 * @param {Object} data - 需求信息
 */
export function createDemand(data) {
  return request.post('/demand', data);
}

/**
 * 获取我的需求列表
 */
export function getMyDemands() {
  return request.get('/demand/my');
}

/**
 * 获取需求详情
 * @param {number} id - 需求ID
 */
export function getDemandDetail(id) {
  return request.get(`/demand/${id}`);
}

/**
 * 更新需求
 * @param {number} id - 需求ID
 * @param {Object} data - 更新数据
 */
export function updateDemand(id, data) {
  return request.put(`/demand/${id}`, data);
}

/**
 * 关闭需求
 * @param {number} id - 需求ID
 */
export function closeDemand(id) {
  return request.put(`/demand/${id}/close`);
}

/**
 * 添加学生信息
 * @param {Object} data - 学生信息
 */
export function addStudent(data) {
  return request.post('/student', data);
}

/**
 * 获取学生列表
 */
export function getStudents() {
  return request.get('/student/my');
}
