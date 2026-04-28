/**
 * 认证相关 API (共享模块)
 */
import request from './request';

/**
 * 用户登录
 * @param {Object} data - { account, password, code, loginType }
 */
export function login(data) {
  return request.post('/auth/login', {
    account: data.account,
    password: data.password,
    code: data.code,
    loginType: data.loginType || 'password'
  });
}

/**
 * 用户注册
 * @param {Object} data - { phone, password, code, role, nickname }
 */
export function register(data) {
  return request.post('/auth/register', data);
}

/**
 * 发送验证码
 * @param {string} phone - 手机号
 * @param {string} purpose - 用途 (register/reset_password)
 */
export function sendCode(phone, purpose = 'register') {
  return request.post('/auth/send-code', null, {
    params: { phone, purpose }
  });
}

/**
 * 重置密码
 * @param {Object} data - { phone, code, newPassword }
 */
export function resetPassword(data) {
  return request.post('/auth/reset/password', data);
}
