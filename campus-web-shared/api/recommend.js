/**
 * 推荐服务 API (共享模块)
 * 协同过滤推荐相关接口
 */
import request from './request'

export function getSimilarTutors(tutorId, limit = 6) {
  return request.get(`/recommend/similar/${tutorId}`, { params: { limit } })
}

export function getPersonalizedRecommendations(limit = 10) {
  return request.get('/recommend/personalized', { params: { limit } })
}
