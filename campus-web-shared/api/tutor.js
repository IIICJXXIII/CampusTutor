/**
 * 教员相关 API (共享模块)
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
 * 获取当前教员档案
 */
export function getTutorProfile() {
  return request.get('/tutor/profile', {
    params: { _t: new Date().getTime() }
  });
}

/**
 * 获取公开教员档案 (家长查看)
 * @param {number} userId - 用户ID
 */
export function getPublicTutorProfile(userId) {
  return request.get(`/tutor/public/${userId}`);
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
 * 保存教员排课配置
 * @param {Array} schedules - 排课配置列表
 */
export function saveScheduleConfig(schedules) {
  return request.post('/tutor/schedule', { schedules });
}

export function getTutorStats() {
  return request.get('/tutor/stats');
}

export function getPublicTutorProfileById(userId) {
  return request.get(`/tutor/public/${userId}`);
}
