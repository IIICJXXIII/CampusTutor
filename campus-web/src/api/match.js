/**
 * 匹配相关 API
 */
import request from './request';

/**
 * 智能匹配教员
 * @param {Object} params - { demandId, latitude, longitude, page, size }
 */
export function matchTutors(params) {
  return request.get('/match/tutors', { params });
}

/**
 * 地图模式查找附近需求 (教员端)
 * @param {Object} params - { latitude, longitude, radius, subject }
 */
export function findNearbyDemands(params) {
  return request.get('/match/demands/nearby', { params });
}

/**
 * 获取教员详情
 * @param {number} tutorId - 教员ID
 */
export function getTutorDetail(tutorId) {
  return request.get(`/match/tutor/${tutorId}`);
}
