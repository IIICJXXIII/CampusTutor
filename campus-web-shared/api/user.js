/**
 * 用户相关 API (共享模块)
 */
import request from './request';

/**
 * 获取用户信息
 * @param {number} id - 用户ID
 */
export function getUserById(id) {
  return request.get(`/user/${id}`);
}

/**
 * 根据用户名获取用户
 * @param {string} username - 用户名
 */
export function getUserByUsername(username) {
  return request.get(`/user/username/${username}`);
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 */
export function updateUser(data) {
  return request.put('/user/', data);
}

/**
 * 更新用户状态 (管理员)
 * @param {number} id - 用户ID
 * @param {number} status - 状态
 */
export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, null, {
    params: { status }
  });
}

/**
 * 修改密码
 * @param {Object} data - 密码信息 { oldPassword, newPassword }
 */
export function updatePassword(data) {
  return request.put('/user/password', data);
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get('/user/current');
}

/**
 * 获取当前用户信息 (别名)
 */
export function getUserInfo() {
  return request.get('/user/current');
}

/**
 * 更新个人信息
 * @param {Object} data - 用户信息
 */
export function updateUserInfo(data) {
  return request.put('/user/info', data);
}
