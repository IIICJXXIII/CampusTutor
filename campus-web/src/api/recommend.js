/**
 * 推荐服务 API
 * 协同过滤推荐相关接口
 */
import request from './request'

/**
 * 获取相似教员推荐
 * "看过这个教员的用户还看过..."
 * @param {number} tutorId 教员ID
 * @param {number} limit 返回数量
 */
export function getSimilarTutors(tutorId, limit = 6) {
    return request({
        url: `/recommend/similar/${tutorId}`,
        method: 'get',
        params: { limit }
    })
}

/**
 * 获取个性化推荐
 * 基于用户历史行为的推荐
 * @param {number} limit 返回数量
 */
export function getPersonalizedRecommendations(limit = 10) {
    return request({
        url: '/recommend/personalized',
        method: 'get',
        params: { limit }
    })
}
