import request from './request';

/**
 * 获取用户信息
 * @param {string|number} id - 用户ID
 */
export function getUserInfo(id) {
  return request.get(`/user/${id}`);
}

/**
 * 根据用户名获取用户信息
 * @param {string} username - 用户名
 */
export function getUserByUsername(username) {
  return request.get(`/user/username/${username}`);
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息对象
 */
export function updateUserInfo(data) {
  return request.put('/user', data);
}

/**
 * 更新用户状态
 * @param {string|number} id - 用户ID
 * @param {number} status - 状态: 1-正常, 0-禁用
 */
export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, null, {
    params: { status }
  });
}
