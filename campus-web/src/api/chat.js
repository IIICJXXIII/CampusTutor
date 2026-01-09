/**
 * 聊天模块 API
 */
import request from './request';

/**
 * 获取会话列表
 */
export function getSessionList() {
    return request.get('/chat/sessions');
}

/**
 * 获取聊天历史
 * @param {number} targetUserId 对方用户ID
 * @param {number} page 页码
 * @param {number} size 每页大小
 */
export function getChatHistory(targetUserId, page = 1, size = 50) {
    return request.get(`/chat/history/${targetUserId}`, {
        params: { page, size }
    });
}

/**
 * 标记消息已读
 * @param {number} targetUserId 对方用户ID
 */
export function markAsRead(targetUserId) {
    return request.post(`/chat/read/${targetUserId}`);
}

/**
 * 获取未读消息数
 */
export function getUnreadCount() {
    return request.get('/chat/unread-count');
}

/**
 * 获取聊天用户信息
 * @param {number} userId 用户ID
 */
export function getChatUserInfo(userId) {
    return request.get(`/chat/user-info/${userId}`);
}
