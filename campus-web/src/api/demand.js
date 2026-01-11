/**
 * 需求相关 API
 */
import request from './request';

/**
 * 发布需求
 * @param {Object} data - 需求信息
 */
export function createDemand(data) {
  return request.post('/demand/publish', data);
}

/**
 * 获取我的需求列表
 */
export function getMyDemands() {
  return request.get('/demand/my');
}

/**
 * 获取需求列表 (公开)
 * @param {Object} params - 查询参数
 */
export function getDemandList(params = {}) {
  return request.get('/demand/list', { params });
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
  // 后端实际接口为 PUT /demand/update，参数体携带 id
  return request.put('/demand/update', { id, ...data });
}

/**
 * 关闭需求
 * @param {number} id - 需求ID
 */
export function closeDemand(id) {
  // 后端只有上下架接口，这里使用下架
  return request.post(`/demand/${id}/offline`);
}

/**
 * 添加学生信息
 * @param {Object} data - 学生信息
 */
export function addStudent(data) {
  return request.post('/parent/student', data);
}

/**
 * 获取学生列表
 */
export function getStudents() {
  return request.get('/parent/students');
}
