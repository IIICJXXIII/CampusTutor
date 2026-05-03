import request from './request';

// ==================== 帖子相关 ====================

/** 获取帖子列表（用户端） */
export function getCommunityPosts(params) {
  return request.get('/community/posts', { params });
}

/** 获取帖子详情 */
export function getCommunityPostDetail(id) {
  return request.get(`/community/posts/${id}`);
}

/** 发布帖子 */
export function createCommunityPost(data) {
  return request.post('/community/posts', data);
}

/** 点赞/取消点赞帖子 */
export function likeCommunityPost(id) {
  return request.post(`/community/posts/${id}/like`);
}

// ==================== 评论相关 ====================

export function getCommunityReplies(postId, params) {
  return request.get(`/community/posts/${postId}/replies`, { params });
}

export function createCommunityReply(postId, data) {
  return request.post(`/community/posts/${postId}/replies`, data);
}

export function getSubReplies(rootId, params = {}) {
  return request.get(`/community/replies/${rootId}/sub`, { params });
}

export function deleteCommunityReply(replyId) {
  return request.delete(`/community/replies/${replyId}`);
}

export function likeCommunityReply(replyId) {
  return request.post(`/community/replies/${replyId}/like`);
}

// ==================== 管理端 ====================

/** 管理员获取帖子列表 */
export function adminGetCommunityPosts(params) {
  return request.get('/admin/community/posts', { params });
}

/** 管理员删除帖子（软删除） */
export function adminDeleteCommunityPost(id) {
  return request.delete(`/admin/community/posts/${id}`);
}

/** 管理员恢复帖子 */
export function adminRestoreCommunityPost(id) {
  return request.put(`/admin/community/posts/${id}/restore`);
}

// ==================== 互动通知 ====================

/** 获取互动通知列表 */
export function getCommunityNotifications(params) {
  return request.get('/community/notifications', { params });
}

/** 获取未读通知数 */
export function getUnreadNotificationCount() {
  return request.get('/community/notifications/unread-count');
}

/** 标记通知已读 */
export function markNotificationRead(id) {
  return request.put(`/community/notifications/${id}/read`);
}

/** 全部标记已读 */
export function markAllNotificationsRead() {
  return request.put('/community/notifications/read-all');
}

// ==================== 常量 ====================

/** 可选标签列表 */
export const COMMUNITY_TAGS = [
  '学习经验',
  '考试技巧',
  '选课建议',
  '校园生活',
  '活动推荐',
  '求助问答'
];

/** 话题类型映射 */
export const TOPIC_TYPE_MAP = {
  1: '经验分享',
  2: '难题求助'
};
