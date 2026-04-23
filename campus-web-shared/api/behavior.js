/**
 * 用户行为上报 API (共享模块)
 * 用于追踪用户行为，支持推荐算法优化
 */
import request from './request'

export function recordView(tutorId, duration = null) {
  return request.post('/behavior/view', { targetId: tutorId, duration })
}

export function recordFavorite(tutorId) {
  return request.post('/behavior/favorite', { targetId: tutorId })
}

export function recordChat(tutorId) {
  return request.post('/behavior/chat', { targetId: tutorId })
}

export function recordSearch() {
  return request.post('/behavior/search')
}

export function getTutorBehaviorStats(tutorId) {
  return request.get(`/behavior/tutor-stats/${tutorId}`)
}
