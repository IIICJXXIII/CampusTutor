/**
 * 家长相关 API (共享模块)
 */
import request from './request';

/**
 * 添加学生
 * @param {Object} data - 学生信息
 */
export function addStudent(data) {
  return request.post('/parent/student', data);
}

/**
 * 更新学生信息
 * @param {Object} data - 学生信息
 */
export function updateStudent(data) {
  return request.put('/parent/student', data);
}

/**
 * 删除学生
 * @param {number} id - 学生ID
 */
export function deleteStudent(id) {
  return request.delete(`/parent/student/${id}`);
}

/**
 * 获取学生列表
 */
export function getStudentList() {
  return request.get('/parent/students');
}

/**
 * 获取学生详情
 * @param {number} id - 学生ID
 */
export function getStudentDetail(id) {
  return request.get(`/parent/student/${id}`);
}
