/**
 * 聊天相关 API (共享模块)
 */
import request from './request';

/**
 * 获取会话列表
 */
export function getConversations() {
  return request.get('/chat/conversations');
}

/**
 * 获取聊天历史
 * @param {number} targetUserId - 对方用户ID
 * @param {Object} params - 分页参数
 */
export function getChatHistory(targetUserId, params) {
  return request.get(`/chat/history/${targetUserId}`, { params });
}

/**
 * 标记消息已读
 * @param {number} targetUserId - 对方用户ID
 */
export function markAsRead(targetUserId) {
  return request.post(`/chat/read/${targetUserId}`);
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount() {
  return request.get('/chat/unread');
}

/**
 * 获取聊天用户信息
 * @param {number} userId - 用户ID
 */
export function getChatUserInfo(userId) {
  return request.get(`/chat/user/${userId}`);
}

/**
 * WebSocket 连接配置
 */
export const WS_CHAT_URL = '/ws/chat';
