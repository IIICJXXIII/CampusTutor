/**
 * 教员相关 API
 */
import request from './request';

/**
 * 获取教员认证信息
 */
export function getCertification() {
  return request.get('/tutor/certification');
}

/**
 * 提交教员认证
 * @param {Object} data - 认证信息
 */
export function submitCertification(data) {
  return request.post('/tutor/certification', data);
}

/**
 * 获取教员档案
 */
export function getTutorProfile() {
  return request.get('/tutor/profile');
}

/**
 * 更新教员档案
 * @param {Object} data - 档案信息
 */
export function updateTutorProfile(data) {
  return request.put('/tutor/profile', data);
}

/**
 * 获取教员排课配置
 */
export function getScheduleConfig() {
  return request.get('/tutor/schedule');
}

/**
 * 更新教员排课配置
 * @param {Array} schedules - 排课配置列表
 */
export function updateScheduleConfig(schedules) {
  return request.put('/tutor/schedule', schedules);
}
