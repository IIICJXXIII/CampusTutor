import request from './request'

// ==================== 帖子相关 ====================

export function getCommunityPosts(params = {}) {
  return request.get('/community/posts', { params })
}

export function getCommunityPostDetail(id) {
  return request.get(`/community/posts/${id}`)
}

export function createCommunityPost(data) {
  return request.post('/community/posts', data)
}

export function likeCommunityPost(id) {
  return request.post(`/community/posts/${id}/like`)
}

// ==================== 评论相关 ====================

export function getCommunityReplies(postId, params = {}) {
  return request.get(`/community/posts/${postId}/replies`, { params })
}

export function createCommunityReply(postId, data) {
  return request.post(`/community/posts/${postId}/replies`, data)
}

export function getSubReplies(rootId, params = {}) {
  return request.get(`/community/replies/${rootId}/sub`, { params })
}

export function deleteCommunityReply(replyId) {
  return request.delete(`/community/replies/${replyId}`)
}

export function likeCommunityReply(replyId) {
  return request.post(`/community/replies/${replyId}/like`)
}

// ==================== 互动通知 ====================

export function getCommunityNotifications(params) {
  return request.get('/community/notifications', { params })
}

export function getUnreadNotificationCount() {
  return request.get('/community/notifications/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/community/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/community/notifications/read-all')
}

// ==================== 管理端 ====================

export function adminGetCommunityPosts(params) {
  return request.get('/admin/community/posts', { params })
}

export function adminDeleteCommunityPost(id) {
  return request.delete(`/admin/community/posts/${id}`)
}

export function adminRestoreCommunityPost(id) {
  return request.put(`/admin/community/posts/${id}/restore`)
}

// ==================== 常量 ====================

export const COMMUNITY_TAGS = [
  '学习经验',
  '考试技巧',
  '选课建议',
  '校园生活',
  '活动推荐',
  '求助问答'
]

export const TOPIC_TYPE_MAP = {
  1: '经验分享',
  2: '难题求助'
}
