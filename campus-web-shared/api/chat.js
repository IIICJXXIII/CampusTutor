/**
 * 聊天相关 API (共享模块)
 */
import request from './request';

/**
 * 获取会话列表
 */
export function getConversations() {
  return request.get('/chat/sessions');
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
  return request.get('/chat/unread-count');
}

/**
 * 获取聊天用户信息
 * @param {number} userId - 用户ID
 */
export function getChatUserInfo(userId) {
  return request.get(`/chat/user-info/${userId}`);
}

/**
 * 获取聊天消息列表
 * @param {number} conversationId - 会话ID
 * @param {Object} params - 分页参数
 */
export function getMessages(conversationId, params) {
  return request.get(`/chat/messages/${conversationId}`, { params });
}

/**
 * 发送消息
 * @param {Object} data - 消息内容
 */
export function sendMessage(data) {
  return request.post('/chat/send', data);
}

/**
 * WebSocket 连接配置
 */
export const WS_CHAT_URL = '/ws/chat';
