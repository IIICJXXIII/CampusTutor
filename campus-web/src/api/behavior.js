/**
 * 用户行为上报 API
 * 用于追踪用户行为，支持推荐算法优化
 */
import request from './request'

/**
 * 记录查看教员详情行为
 * @param {number} tutorId 教员ID
 * @param {number} duration 停留时长（秒），可选
 */
export function recordView(tutorId, duration = null) {
    return request({
        url: '/behavior/view',
        method: 'post',
        data: { targetId: tutorId, duration }
    })
}

/**
 * 记录收藏教员行为
 * @param {number} tutorId 教员ID
 */
export function recordFavorite(tutorId) {
    return request({
        url: '/behavior/favorite',
        method: 'post',
        data: { targetId: tutorId }
    })
}

/**
 * 记录发起聊天行为
 * @param {number} tutorId 教员ID
 */
export function recordChat(tutorId) {
    return request({
        url: '/behavior/chat',
        method: 'post',
        data: { targetId: tutorId }
    })
}

/**
 * 记录搜索行为
 */
export function recordSearch() {
    return request({
        url: '/behavior/search',
        method: 'post'
    })
}

/**
 * 获取教员热度统计（调试用）
 * @param {number} tutorId 教员ID
 */
export function getTutorStats(tutorId) {
    return request({
        url: `/behavior/tutor-stats/${tutorId}`,
        method: 'get'
    })
}
